# AimAware

AimAware is a prototype machine learning pipeline for real-time detection of aimbot behavior primarily in Minecraft 1.8, concept applicable to other versions & games. It analyzes player aiming patterns to distinguish human users from automated aiming software. Visient operates as an end-to-end ML detection prototype, from data collection to live inference.

---

## Project Structure

```
Visient/
├── visient-training/
│     ├── visient_model.pkl
│     ├── visient_scaler.pkl
│     ├── trained_feature_order.pkl
│     └── visient_data.json
└── visient-server/
      └── server.py
```

---

## Features

- Real-time FastAPI inference server
- Statistical feature extraction, including:
  - mean
  - standard deviation
  - skewness
  - autocorrelation
  - entropy
  - energy
  - zero crossings
  - etc
- Trained on human and aimbot gameplay data
- Achieves approximately 80% cross-validation accuracy
- API endpoint for JSON-based data collection

---

## Setup instructions

1. Clone the repository:

    ```
    git clone https://github.com/yourusername/Visient.git
    cd Visient
    ```

2. Create and activate a virtual environment:

    ```
    python -m venv venv
    source venv/bin/activate   # Windows: venv\Scripts\activate
    ```

3. Install dependencies:

    ```
    pip install -r requirements.txt
    ```

4. Ensure the following model files exist in `visient-training/`:
   - visient_model.pkl
   - visient_scaler.pkl
   - trained_feature_order.pkl

*(If these files are unavailable, you will need to train new models or obtain them separately.)*

---

## Running the server

From the `visient-server/` directory:

```
uvicorn server:app --reload
```

API available at:

```
http://127.0.0.1:8000
```

---

## API endpoints

### POST /predict

Predicts whether input data represents human or aimbot behavior.

Example request:

```json
{
  "features": {
    "deltaYaw_mean": 0.15,
    "deltaYaw_std": 0.02,
    "deltaYaw_skewness": -0.3
  }
}
```

Example response:

```json
{
  "prediction": "human",
  "confidence": 0.84
}
```

---

### POST /collect

Collects gameplay data for future model training.

Example request:

```json
{
  "uuid": "player-1234",
  "config": "test-config",
  "signals": {...},
  "features": {...}
}
```

Collected data is saved to:

```
visient-training/visient_data.json
```

---

## Known limitations

- Currently trained primarily on data from a single player
- Limited diversity in aimbot configurations
- Model performance on unseen human behaviors or new aimbots may be reduced

---

## Future improvements

- Collect and incorporate data from multiple players for better generalization
- Expand feature set with additional statistical or time-based metrics
- Improve detection of unseen aimbot behaviors
- Implement automated retraining pipelines
- Implement modern version support

---

## License

MIT License.

---
