from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import json
import pandas as pd
import os

# ------------------------------
# Load model and scaler
# ------------------------------

# Get current file path
server_dir = os.path.dirname(__file__)

# Go one directory up
project_root = os.path.abspath(os.path.join(server_dir, ".."))

# Point to aimaware-training
training_dir = os.path.join(project_root, "aimaware-training")

# Create data file path
data_file_path = os.path.join(training_dir, "aimaware_data.jsonl")

# Now load model related files
model = joblib.load(os.path.join(training_dir, "aimaware_model.pkl"))
scaler = joblib.load(os.path.join(training_dir, "aimaware_scaler.pkl"))
feature_order = joblib.load(os.path.join(training_dir, "trained_feature_order.pkl"))

# ------------------------------
# Define FastAPI app
# ------------------------------

app = FastAPI()

# ------------------------------
# Model diagnostics for debugging
# ------------------------------

@app.on_event("startup")
def model_diagnostics():
    print("Inference server initialized.")

    # Model info
    print(f"Model type: {type(model).__name__}")
    if hasattr(model, 'classes_'):
        print(f"Classes: {model.classes_}")
    if hasattr(model, 'n_features_in_'):
        print(f"Input features: {model.n_features_in_}")

    # Scaler info
    print(f"Scaler type: {type(scaler).__name__}")
    if hasattr(scaler, 'n_features_in_'):
        print(f"Scaler expects: {scaler.n_features_in_} features")

    # Try to print feature names if possible
    if hasattr(scaler, 'feature_names_in_'):
        print(f"Feature columns: {list(scaler.feature_names_in_)}")
    else:
        print("Feature columns: (not available)")


# ------------------------------
# Data structures
# ------------------------------

class PredictRequest(BaseModel):
    uuid: str
    features: dict[str, float]


class CollectRequest(BaseModel):
    uuid: str
    config: str
    signals: dict[str, list[float]]
    features: dict[str, float]


# ------------------------------
# Endpoint for live inferencing on new data.
# ------------------------------

@app.post("/predict")
def predict(request: PredictRequest):
    try:

        # Get expected feature order from scaler
        feature_order = list(scaler.feature_names_in_)

        # Extract feature values in correct order
        feature_values = [request.features[name] for name in feature_order]

        # Create a DataFrame to preserve column names for StandardScaler
        X_df = pd.DataFrame([feature_values], columns=feature_order)

        # Scale the input using the trained scaler
        X_scaled = scaler.transform(X_df)

        # Predict
        prediction = model.predict(X_scaled)[0]
        probabilities = model.predict_proba(X_scaled)[0]

        return {
            "uuid": request.uuid,
            "prediction": int(prediction),
            "probability": probabilities.tolist()
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ------------------------------
# Endpoint for collecting data into the dataset
# ------------------------------

@app.post("/collect")
def collect(request: CollectRequest):
    try:
        record = {
            "uuid": request.uuid,
            "config": request.config,
            "signals": request.signals,
            "features": request.features
        }

        # Collect the data as JSON
        with open(data_file_path, "a") as f:
            json.dump(record, f)
            f.write("\n")

        return {"status": "saved", "uuid": request.uuid}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ------------------------------
# Run this to start the AimAware server.
# ------------------------------

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("server:app", host="127.0.0.1", port=8000, reload=True)