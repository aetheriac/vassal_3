SRCDIR:=src
LIBDIR:=lib
CLASSDIR:=classes
TMPDIR:=tmp
JDOCDIR:=javadoc
DOCDIR:=doc
DISTDIR:=dist

VERSION:=3.1.0-svn$(shell svnversion | perl -pe 's/(\d+:)?(\d+[MS]?)/$$2/; s/(\d+)M/$$1+1/e')

CLASSPATH:=$(CLASSDIR):$(LIBDIR)/*
JAVAPATH:=/usr/lib/jvm/java-1.6.0-sun

#CLASSPATH:=$(CLASSDIR):$(shell echo $(LIBDIR)/*.jar | tr ' ' ':')
#JAVAPATH:=/usr/lib/jvm/java-1.5.0-sun

JC:=$(JAVAPATH)/bin/javac
JCFLAGS:=-d $(CLASSDIR) -source 5 -target 5 -Xlint -classpath $(CLASSPATH) \
				 -sourcepath $(SRCDIR)

JAR:=$(JAVAPATH)/bin/jar
JDOC:=$(JAVAPATH)/bin/javadoc

NSIS:=PATH=$$PATH:/home/uckelman/java/nsis-2.35/ makensis

SOURCES:=$(shell find $(SRCDIR) -name '*.java' | sed "s/^$(SRCDIR)\///")
CLASSES:=$(SOURCES:.java=.class)
JARS:=Vengine.jar docs.jar

vpath %.class $(shell find $(CLASSDIR) -type d)
vpath %.java  $(shell find $(SRCDIR) -type d -name .svn -prune -o -print)
vpath %.jar $(LIBDIR)

all: $(CLASSDIR) $(CLASSES) i18n images

$(CLASSDIR):
	mkdir -p $(CLASSDIR)

%.class: %.java
	$(JC) $(JCFLAGS) $<

images: $(CLASSDIR)/images

$(CLASSDIR)/images: $(CLASSDIR)
	svn export $(SRCDIR)/images $(CLASSDIR)/images

i18n: $(CLASSDIR)
	for i in `cd $(SRCDIR) && find VASSAL -name '*.properties'`; do cp $(SRCDIR)/$$i $(CLASSDIR)/$$i; done

#fast:
#	$(JC) $(JCFLAGS) $(shell find $(SRCDIR) -name '*.java')

#show:
#	echo $(patsubst %,-C $(TMPDIR)/doc %,$(wildcard $(TMPDIR)/doc/*)) 

Vengine.jar: all
	$(JAR) cvfm $(LIBDIR)/$@ dist/Vengine.mf -C $(CLASSDIR) .

docs.jar:
	mkdir -p $(TMPDIR)
	svn export $(DOCDIR) $(TMPDIR)/doc
	find $(TMPDIR)/doc -type f | sed "s/^$(TMPDIR)\/doc\///" >$(TMPDIR)/doc/docsList
	cp dist/docsInfo $(TMPDIR)/doc
	$(JAR) cvf $(LIBDIR)/$@ -C $(TMPDIR)/doc .

version:
	sed -ri 's/VERSION = ".*"/VERSION = "$(VERSION)"/' $(SRCDIR)/VASSAL/Info.java

$(TMPDIR)/VASSAL-$(VERSION).app: version all $(JARS)
	mkdir -p $(TMPDIR)/VASSAL-$(VERSION).app/Contents/{MacOS,Resources}
	cp dist/{PkgInfo,Info.plist} $(TMPDIR)/VASSAL-$(VERSION).app/Contents
	cp dist/JavaApplicationStub $(TMPDIR)/VASSAL-$(VERSION).app/Contents/MacOS
	svn export $(LIBDIR) $(TMPDIR)/VASSAL-$(VERSION).app/Contents/Resources/Java
	cp $(LIBDIR)/{Vengine.jar,docs.jar} $(TMPDIR)/VASSAL-$(VERSION).app/Contents/Resources/Java

$(TMPDIR)/VASSAL-$(VERSION).dmg: $(TMPDIR)/VASSAL-$(VERSION).app
	dd if=/dev/zero of=$(TMPDIR)/VASSAL-$(VERSION).dmg bs=1M count=$$(( `du -s $(TMPDIR)/VASSAL-$(VERSION).app/ | sed 's/\s\+.*$$//'` / 1024 + 1 ))
	mkfs.hfsplus -s -v VASSAL-$(VERSION) $(TMPDIR)/VASSAL-$(VERSION).dmg
	mkdir -p $(TMPDIR)/dmg
	sudo sh -c "mount -t hfsplus -o loop $(TMPDIR)/VASSAL-$(VERSION).dmg $(TMPDIR)/dmg ; cp -va $(TMPDIR)/VASSAL-$(VERSION).app $(TMPDIR)/dmg ; umount $(TMPDIR)/dmg"
	rmdir $(TMPDIR)/dmg

$(TMPDIR)/VASSAL-$(VERSION).zip: version all $(JARS) 
	mkdir -p $(TMPDIR)/VASSAL-$(VERSION)
	svn export $(LIBDIR) $(TMPDIR)/VASSAL-$(VERSION)/lib
	cp $(LIBDIR)/{Vengine.jar,docs.jar} $(TMPDIR)/VASSAL-$(VERSION)/lib
	cp dist/VASSAL{Editor,}.{sh,bat,exe} $(TMPDIR)/VASSAL-$(VERSION)
	cd $(TMPDIR) ; zip -9rv VASSAL-$(VERSION).zip VASSAL-$(VERSION) ; cd ..

$(TMPDIR)/VASSAL-$(VERSION)-windows.exe: release-generic
	$(NSIS) -NOCD -DVERSION=$(VERSION) -DTMPDIR=$(TMPDIR) dist/VASSAL.nsi

release-macosx: $(TMPDIR)/VASSAL-$(VERSION).dmg

release-windows: $(TMPDIR)/VASSAL-$(VERSION)-windows.exe

release-generic: $(TMPDIR)/VASSAL-$(VERSION).zip

release: release-generic release-windows release-macosx

clean-release:
	$(RM) -r $(TMPDIR)/* $(LIBDIR)/Vengine.jar $(LIBDIR)/docs.jar

javadoc:
	$(JDOC) -d $(JDOCDIR) -link http://java.sun.com/javase/6/docs/api -sourcepath $(SRCDIR) -subpackages $(shell echo $(notdir $(wildcard src/*)) | tr ' ' ':')

clean-javadoc:
	$(RM) -r $(JDOCDIR)

clean: clean-release
	$(RM) -r $(CLASSDIR)/*

.PHONY: all clean release release-macosx release-windows release-generic clean-release i18n images javadoc clean-javadoc version
