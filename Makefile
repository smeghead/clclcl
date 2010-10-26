VERSION=0.1.1-beta
DIST_DIR=dist/clclcl-${VERSION}

default:
	mv resources/.svn resources/svn
	mv resources/clclcl/.svn resources/clclcl/svn
	lein uberjar
	mv resources/svn resources/.svn
	mv resources/clclcl/svn resources/clclcl/.svn

dist: default
	mkdir -p ${DIST_DIR}
	cp ChangeLog README clclcl clclcl-menu-open logging.properties clclcl.bat clclcl-standalone.jar resources/clclcl/clclcl.png ${DIST_DIR}
	chmod +x ${DIST_DIR}/clclcl
	cd dist && zip -r clclcl-${VERSION}.zip clclcl-${VERSION}

clean:
	rm -rf classes/* clclcl*.jar
