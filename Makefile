PROTOS := src/main/proto

.PHONY: proto
proto: $(PROTOS)
	cp -r lib/explorer/api/ src/main/proto
	rm -rf src/main/proto/platformd

$(PROTOS):
	mkdir -p $(PROTOS)