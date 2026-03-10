.PHONY: build run test lint format

build:
	./gradlew build

run:
	./gradlew run

test:
	./gradlew test

lint:
	./gradlew ktlintCheck detekt

format:
	./gradlew ktlintFormat