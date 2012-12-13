Utility for download updates for NOD32 from official update site.
You may download compiled jar from [Amazon S3](https://s3.amazonaws.com/eset_upd/name/valery1707/tools/eset_upd/0.1.0/eset_upd-0.1.0-onejar.jar) or create jar by youself (mvn clean package).

After first run in directory with jar will be created configuration files (config.ini, logback4j.xml).
You must configure values in config.ini, and may configure logback4j.xml.

Use 2 directories:
1. path.tmp - Store file while it's loaded from internet
2. path.web - Store real update mirror
User, who run script, must have rights to write in to both directories.
