VERSION=$(shell head -1 project.clj | sed -e 's/[^"]*"\([^"]*\)"[^"]*/\1/')
DIST_DIR=dist/clclcl-${VERSION}

default:
	lein uberjar

dist: default
	mkdir -p ${DIST_DIR}
	cp ChangeLog README.md clclcl clclcl-menu-open logging.properties clclcl.bat clclcl-${VERSION}-standalone.jar resources/clclcl/clclcl.png ${DIST_DIR}
	chmod +x ${DIST_DIR}/clclcl
	cd dist && zip -r clclcl-${VERSION}.zip clclcl-${VERSION}

clean:
	rm -rf classes/* clclcl*.jar

vtest:
	echo ${VERSION}
