# jdbcSapStatus

# Check Health SAP HANA via JDBC and save in Elastic

Files:
- **jdbcSapStatus.java**: File with code.
- **hosts_hana.txt**: Hana's to check, format: [ **host:port user password** ]
- **jdbcSapStatus.jar**: Jar to download and run... (needed hosts_hana.txt)

# My example and case of use:

Include jar into crontab to check every 5 minutes the health, save in Elastic, and show in Kibana.

> Create a folder: "HanaStatus" in /opt/ for example and include into crontab

```sh
*/5 * * * * cd /opt/HanaStatus/ && java -cp "/opt/HanaStatus/:/opt/HanaStatus/jdbcSapStatus.jar:/opt/HanaStatus/ngdbc.jar" jdbcSapStatus
```

# Create jar file

```sh
jar cfe jdbcSapStatus.jar jdbcSapStatus jdbcSapStatus.class
```

In my case i used the ngdbc.jar library in the same folder, because on the installation of SAP Client the administrator not installed the jdbc libraries.

Check driver installation:

> The driver (ngdbc.jar) is installed as part of the SAP HANA client installation and is located at:
>    C:\Program Files\sap\hdbclient\ on Microsoft Windows platforms
>    /usr/sap/hdbclient/ on Linux and UNIX platforms

Example json in Kibana:

```json
{
  "_index": "monitor-2018.06.13",
  "_type": "fluentd",
  "_id": "XXXXXXX",
  "_version": 1,
  "_score": null,
  "_source": {
    "primary_class": "SAP",
    "secondary_class": "HANA",
    "host": "host:port",
    "message": "Connection to HANA successful!",
    "status": "OK",
    "@timestamp": "2018-06-13T09:50:01.806242367+02:00",
    "tag": "monitor"
  }
}
```

# Based on:
[SAP Help Portal Link](https://help.sap.com/viewer/52715f71adba4aaeb480d946c742d1f6/2.0.00/en-US/ff15928cf5594d78b841fbbe649f04b4.html) - Connect to SAP HANA via JDBC
