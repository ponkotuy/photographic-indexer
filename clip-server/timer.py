import time


class Timer:
    def __init__(self):
        self.t = time.time()

    def show(self, text):
        now = time.time()
        print(text, now - self.t)
        self.t = now
