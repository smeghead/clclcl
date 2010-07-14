VERSION=0.0.4-alpha
DIST_DIR=dist/clclcl-${VERSION}

default:
	lein uberjar

dist: default
	mkdir -p ${DIST_DIR}
	cp ChangeLog README clclcl logging.properties clclcl.bat clclcl-standalone.jar ${DIST_DIR}
	chmod +x ${DIST_DIR}/clclcl
	cd dist && zip -r clclcl-${VERSION}.zip clclcl-${VERSION}

clean:
	rm -rf classes/* clclcl*.jar
