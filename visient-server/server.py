from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import numpy as np

# Load trained model
model = joblib.load("model.pkl")

# Load scaler
scaler = joblib.load("model.pkl")

# Define FastAPI app
app = FastAPI()

# Define the expected request format
class Features(BaseModel):
    data: list[float]

@app.post("/predict")
def predict(features: Features):
    # Convert incoming data to numpy array
    X = np.array([features.data])

    # Scale the input features
    X_scaled = scaler.transform(X)

    # Run inference
    prediction = model.predict(X_scaled)
    probabilities = model.predict_proba(X_scaled)

    # Format response
    return {
        "prediction": int(prediction[0]),
        "probability": probabilities[0].tolist()
    }
