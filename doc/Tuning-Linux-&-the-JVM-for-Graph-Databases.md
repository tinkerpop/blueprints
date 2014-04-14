# Tuning Linux & the JVM for Graph Databases

Tuning the Linux kernel, file system, and JVM options is important to get the best performance out of any database. Here are recommendations and guidelines for different Blueprints databases. These are general best practices, but as always, you may need to adjust them for your use case.

## Neo4j 

### JVM Options
### Linux Kernel
### Linux File System
### EC2

Recommendations by [https://github.com/jexp](Michael Hunger) (9/8/2011)...

**Recommendations for using EBS (confirmed)**
* create a raid0 of up to 8 ebs disks
* use a different scheduler
* Ubuntu Raid performance: http://www.stress-free.co.nz/tuning_ubuntus_software_raid

**Recommendations for using EBS (not yet confirmed)**
* pre initialize all of the volumes with dd
* use a different fs than ext3 (ext2, jfs, xfs)
* large chunk size for raid (256k)
* larger read ahead buffer (256b -> 64k)
* java -server -d64
* test out ec2 instances and ebs volumes and discard the bad ones

/etc/sysctl.conf
```bash
vm.dirty_background_ratio = 50
vm.dirty_ratio = 80
vm.overcommit_memory = 1
```

sysctl -p

Different IO-scheduler:
kernel boot parameter
```
elevator=as 
(anticipatory scheduling)

elevator=deadline
(deadline scheduler)
```

**Tools for monitoring io / latency**
* latencytop.com (from intel, decomposes wait issues)
* gclogs (java -XX:+PrintGCTimeStamps -verbose:gc -Xloggc:gc.log )
* top (iowait, cpu, mem)
* ionice -p <pid> -c 1 -n 7
* mpstat 3
* iostat -m /dev/md0 /dev/md1 3
* vmstat -S M 3
* Bonnie++

### More Documentation
* [Neo4j Configuration & Performance](http://docs.neo4j.org/chunked/stable/embedded-configuration.html)


## OrientDB

* [OrientDB Performance Tuning](https://github.com/nuvolabase/orientdb/wiki/Performance-Tuning)
* [OrientDB Guide to Improving Performance Based on the Use Case](https://code.google.com/p/orient/wiki/PerformanceTuning)

## Titan

### JVM Options

Here are some Java options recommended by [Pavel](https://github.com/xedin)...

Tuning

```bash
-XX:+UseParNewGC
-XX:+UseConcMarkSweepGC
-XX:+CMSParallelRemarkEnabled
-XX:SurvivorRatio=8
-XX:MaxTenuringThreshold=1
-XX:CMSInitiatingOccupancyFraction=75
-XX:+UseCMSInitiatingOccupancyOnly
```

Logging

```bash
-XX:+PrintGCDetails 
-XX:+PrintGCDateStamps 
-XX:+PrintHeapAtGC 
-XX:+PrintTenuringDistribution
-XX:+PrintGCApplicationStoppedTime
-XX:+PrintPromotionFailure
-Xloggc:/var/log/rexster-gc-date +%s.log
```

See https://github.com/tinkerpop/rexster/issues/271#issuecomment-13160693

ElasticSearch also recommends/defaults to pretty much the same options:
https://github.com/elasticsearch/elasticsearch-servicewrapper/blob/master/service/elasticsearch.conf#L40

Here's a detailed post explaining them:
http://jprante.github.io/2012/11/28/Elasticsearch-Java-Virtual-Machine-settings-explained.html

### Linux Kernel
### Linux File System
### EC2
### More Documentation

## Dex

### JVM Options
### Linux Kernel
### Linux File System
### EC2
### More Documentation


## Sail

### JVM Options
### Linux Kernel
### Linux File System
### EC2
### More Documentation