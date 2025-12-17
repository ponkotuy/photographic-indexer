import string
from typing import Union, List

import torch
from PIL import Image
from transformers import AutoImageProcessor, AutoModel, AutoTokenizer

DEVICE = "cpu"
MODEL_PATH = "line-corporation/clip-japanese-base"


class ClipTensor:
    def __init__(self):
        self._tokenizer = AutoTokenizer.from_pretrained(MODEL_PATH, trust_remote_code=True)
        self._processor = AutoImageProcessor.from_pretrained(MODEL_PATH, trust_remote_code=True)
        self._model = AutoModel.from_pretrained(MODEL_PATH, trust_remote_code=True).to(DEVICE)
        self._model.eval()

    def image(self, img: Image) -> torch.Tensor:
        inputs = self._processor(img, return_tensors="pt").to(DEVICE)
        with torch.no_grad():
            features = self._model.get_image_features(**inputs)
        return features[0]

    def text(self, s: string) -> torch.Tensor:
        inputs = self._tokenizer([s], return_tensors="pt").to(DEVICE)
        with torch.no_grad():
            features = self._model.get_text_features(**inputs)
        return features[0]
