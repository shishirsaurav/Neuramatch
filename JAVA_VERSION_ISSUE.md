# Java Version Compatibility Issue

## Problem

You're running **Java 24**, but Lombok 1.18.34 (latest stable) doesn't fully support Java 24 yet.

Current Java version:
```
java version "24.0.2" 2025-07-15
```

## Solution Options

### Option 1: Use Java 21 (Recommended - LTS Version)

Download and install Java 21 from:
- **Oracle JDK 21**: https://www.oracle.com/java/technologies/downloads/#java21
- **OpenJDK 21**: https://adoptium.net/temurin/releases/?version=21

After installation, set JAVA_HOME:

**macOS/Linux:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH
```

Add to `~/.zshrc` or `~/.bash_profile` to make it permanent.

**Verify:**
```bash
java -version
# Should show: java version "21.x.x"
```

Then rebuild:
```bash
cd backend
mvn clean install
```

### Option 2: Use SDKMAN (Easiest - Recommended)

Install SDKMAN:
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

Install Java 21:
```bash
sdk install java 21.0.1-tem
sdk use java 21.0.1-tem
```

Verify and build:
```bash
java -version
cd backend
mvn clean install
```

### Option 3: Keep Java 24 (Experimental)

If you must use Java 24, you'll need Lombok edge releases which are experimental and may be unstable.

**Not recommended for production.**

## Why Java 21?

- ✅ **LTS (Long Term Support)** - Supported until 2029
- ✅ **Fully compatible** with Spring Boot 3.2.0
- ✅ **Fully compatible** with Lombok 1.18.34
- ✅ **Production ready** and stable
- ✅ **Used by most enterprise projects**

Java 24 is a non-LTS release and many libraries haven't caught up yet.

## Current Build Error

```
Fatal error compiling: java.lang.ExceptionInInitializerError:
com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

This error occurs because Lombok's annotation processor doesn't recognize Java 24's internal compiler APIs.
