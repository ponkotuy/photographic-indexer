
import glob

from PIL import Image

from image_preprocess import ImagePreprocess

DIR = '/home/yosuke/picture/exported/20230506/'


def text_grouping(preprocess: ImagePreprocess):
    text_probs = preprocess.text_grouping(['都市', '砂漠', '公園'])
    print("text grouping:", text_probs)


def text_similarity(preprocess: ImagePreprocess):
    similarities = preprocess.text_similarity('砂漠')
    print("similarities", similarities)


if __name__ == "__main__":
    files = glob.glob(DIR + '*')
    files.sort()
    print(files)
    images = [Image.open(f) for f in files]
    processor = ImagePreprocess(images)
    text_similarity(processor)
