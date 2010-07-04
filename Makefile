VERSION=0.0.1-alpha

default:
	lein uberjar

dist: default
	mkdir -p dist/clclcl-${VERSION}
	cp README clclcl clclcl.bat clclcl-standalone.jar dist/clclcl-${VERSION}

