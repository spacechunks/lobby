PROTOS := src/main/proto

.PHONY: proto
proto: $(PROTOS)
	git submodule init
	git submodule update
	cp -r lib/explorer/api/ src/main/proto

$(PROTOS):
	mkdir -p $(PROTOS)