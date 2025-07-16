# AimAware

AimAware is a prototype machine learning pipeline for real-time detection of aimbot behavior in online video games. Currently, AimAware is prototyped using Minecraft 1.8, through the concept is largely applicable to other versions & games. It analyzes player aiming patterns to distinguish human users from automated aiming software. AimAware operates as an end-to-end ML detection prototype, from data collection to live inference.

## Q: "Why prototype using Minecraft 1.8? Isn't that a 10 year old version?"

A: Minecraft 1.8 is over 10 years old, and that's what makes it so great for prototyping. It is a very well researched version with dozens of public resources and anti-cheat systems supporting it. Many servers today still run on Minecraft 1.8, for it's performance and widespread support. I started coding with 1.8 in 2018, so it is an domain where I have great development knowledge. As for other games, aiming behaviour is largely similar, even to FPS games like Counter Strike, the only obstacle is porting the pipeline to work with the protocol of the specified game.

## Q: "How can you be sure it will work with other games?"

A: As for other games, aiming behaviour is largely similar, even to FPS games like Counter Strike, the only obstacle is porting the pipeline to work with the protocol of the specified game.

## Q: "Okay, but aren't heuristic detections more reliable anyway?"

A: No, human made heuristics are not more reliable, simply put. Human made heuristics are based on pseudostatistics, value patching, and arbitrary thresholding, and in most cases, simply theorized based on a singular players' data. ML approaches are much more robust, since production systems take in 100s to 1000s of players aim patterns, and uses mathematically sound approaches to maximize functionality.

## Q: "That makes sense, but the Minecraft anti-cheating industry has seen several ML approaches over the last 13 years and none have worked. What makes AimAware different?"

A: Unlike old ML approaches, AimAware differs greatly in what data is collected. AimAware uses a precise entity tracking system, popularized in 2020, to track exactly where aim targets are on the player's screen.

For a direct comparison of precision:
- AimAware latency compensation: 1*10^-7 blocks
- Classical "rewind-time" latency compensation: 0.5 blocks

Combined with a raytrace system reverse-engineered from the client, AimAware knows exactly where the target is on your screen, and exactly where you are aiming on the target. This makes the raw quality of data fed into AimAware much higher than previous approaches.

For a direct comparison of data quality:

|                   | Old ML designs                                      | AimAware                                                                                  |
|-------------------|-----------------------------------------------------|-------------------------------------------------------------------------------------------|
| Entity tracking   | Not used or inaccurate                              | Tracked with 1×10⁻⁸ blocks of precision                                                   |
| Ray tracing       | Not used                                            | Reverse engineered from the client                                                        |
| Tracking context  | Not used or inaccurate                              | Knows exactly where you are aiming on target over time no matter the latency              |
| Features          | Rotational data (kinematics, raw values)            | Extracts meaningful statistical features from rotational/tracking derivatives             |
| Performance       | False positives and lackluster detection            | 85% accuracy and proven to detect 11 unique aimbots                                       |

---

## Project Structure

```
AimAware/
├── aimaware-training/
│     ├── aimaware_model.pkl
│     ├── aimaware_scaler.pkl
│     ├── trained_feature_order.pkl
│     └── aimaware_data.json
└── aimaware-server/
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
    git clone https://github.com/yourusername/AimAware.git
    cd AimAware
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

4. Ensure the following model files exist in `aimaware-training/`:
   - aimaware_model.pkl
   - aimaware_scaler.pkl
   - trained_feature_order.pkl

*(If these files are unavailable, you will need to train new models or obtain them separately.)*

---

## Running the server

From the `aimaware-server/` directory:

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
aimaware-training/aimaware_data.json
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
