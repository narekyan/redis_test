# IP Address Counter
This program counts unique IPv4 addresses in a large text file using Kotlin. 

## Features
- Memory-efficient with storing ips in redis.

## How to Run
- You have to have redis installed and started
- Run (for example: java -cp jedis-5.1.3.jar:Main.jar MainKt sampleips.txt)
    - `java -cp jedis-5.1.3.jar:Main.jar MainKt <file-path>`
