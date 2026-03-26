.PHONY: build test run clean infra-up infra-down infra-clean verify

build:
	mvn clean package -DskipTests

test:
	mvn test

verify:
	mvn verify

run:
	mvn spring-boot:run -Dspring-boot.run.profiles=dev

clean:
	mvn clean

infra-up:
	docker-compose up -d
	@echo "⏳  Waiting for services …"
	@sleep 5
	@docker-compose ps

infra-down:
	docker-compose down

infra-clean:
	docker-compose down -v
