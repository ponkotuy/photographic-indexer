FROM python:3.11.3-slim-bullseye

ENV POETRY_HOME=/opt/poetry
ENV POETRY_VERSION=1.8.5

RUN apt-get update && \
    apt-get install -y curl pkg-config git make cmake sentencepiece g++ && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN curl -sSL https://install.python-poetry.org | python3 - && \
    cd /usr/local/bin && \
    ln -s /opt/poetry/bin/poetry

WORKDIR /app

COPY ./poetry.lock ./pyproject.toml /app/

RUN poetry install --no-dev --no-root

COPY *.py /app/
COPY ./japanese-cloob-vit-b-16/ /app/japanese-cloob-vit-b-16/

CMD poetry run gunicorn -k uvicorn.workers.UvicornWorker -b 0.0.0.0:8000 main:app
