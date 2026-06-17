PROTOS := src/main/proto

.PHONY: upload
upload:
	./gradlew shadowJar
	mc put build/libs/explorer-lobby.jar hcloud-nbg1/spc-mc/lobby/plugins/lobby.jar

.PHONY: proto
proto: $(PROTOS)
	cp -r lib/explorer/api/ src/main/proto
	rm -rf src/main/proto/platformd
	mkdir src/main/proto/matchmaking
	cp -r lib/matchmaking/api/ src/main/proto/matchmaking

$(PROTOS):
	mkdir -p $(PROTOS)