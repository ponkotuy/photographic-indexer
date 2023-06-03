import json
import os
import string
from typing import Union, List

import japanese_clip
import torch
from PIL import Image
from japanese_clip import CLOOBModel, CLIPModel

DEVICE = "cpu"
MODEL_FILE = "pytorch_model.bin"
CONFIG_FILE = "config.json"
MODEL_CLASSES = {
    "cloob": CLOOBModel,
    "clip": CLIPModel,
}


def _transform(image_size):
    from torchvision import transforms as T
    return T.Compose([
        T.Resize(image_size, interpolation=T.InterpolationMode.BILINEAR),
        T.CenterCrop(image_size),
        lambda image: image.convert('RGB'),
        T.ToTensor(),
        T.Normalize((0.48145466, 0.4578275, 0.40821073), (0.26862954, 0.26130258, 0.27577711), )
    ])


def _open_model(directory, **kwargs):
    with open(os.path.join(directory, CONFIG_FILE), "r", encoding="utf-8") as f:
        j = json.load(f)
    model_class = MODEL_CLASSES[j["model_type"]]
    model = model_class.from_pretrained("rinna/japanese-cloob-vit-b-16", **kwargs)
    model = model.eval().requires_grad_(False).to(DEVICE)
    return model, _transform(model.config.vision_config.image_size)


class ClipTensor:
    def __init__(self):
        self._model, self._preprocess = _open_model("japanese-cloob-vit-b-16")
        self._tokenizer = japanese_clip.load_tokenizer()

    def _text_encode(self, texts: Union[List[string], string]):
        return japanese_clip.tokenize(
            texts=texts,
            max_seq_len=77,
            device=DEVICE,
            tokenizer=self._tokenizer,  # this is optional. if you don't pass, load tokenizer each time
        )

    def image(self, img: Image):
        x = self._preprocess(img).unsqueeze(0).to(DEVICE)
        with torch.no_grad():
            features = self._model.get_image_features(x)
        return features[0]

    def text(self, s: string):
        encodings = self._text_encode(s)
        with torch.no_grad():
            features = self._model.get_text_features(**encodings)
        return features[0]
