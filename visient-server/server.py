from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import numpy as np
import json

# Load trained model
model = joblib.load("model.pkl")

# Load scaler
scaler = joblib.load("scaler.pkl")

# Define FastAPI app
app = FastAPI()


# ------------------------------
# DATA MODELS
# ------------------------------

class PredictRequest(BaseModel):
    uuid: str
    features: dict[str, float]


class CollectRequest(BaseModel):
    uuid: str
    signals: dict[str, list[float]]
    features: dict[str, float]


# ------------------------------
# INFERENCE ENDPOINT
# ------------------------------

@app.post("/predict")
def predict(request: PredictRequest):
    try:
        # Convert features dict to array (ordered by key for consistency)
        feature_names = sorted(request.features.keys())
        feature_values = [request.features[name] for name in feature_names]

        X = np.array([feature_values])

        # Scale input
        X_scaled = scaler.transform(X)

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
# COLLECTION ENDPOINT
# ------------------------------

@app.post("/collect")
def collect(request: CollectRequest):
    try:
        # For now, just save each record to a JSONL file
        record = {
            "uuid": request.uuid,
            "signals": request.raw_signals,
            "features": request.features
        }

        # Append to file
        with open("visient_data.jsonl", "a") as f:
            json.dump(record, f)
            f.write("\n")

        return {"status": "saved", "uuid": request.uuid}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
