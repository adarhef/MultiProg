JFLAGS= 
JC= javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	PaddedPrimitive.java \
	StopWatch.java \
	Fingerprint.java \
	RandomGenerator.java \
	PacketWorker.java \
	Firewall.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
