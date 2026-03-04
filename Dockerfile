FROM ubuntu:latest
LABEL authors="ivansapronov"

ENTRYPOINT ["top", "-b"]