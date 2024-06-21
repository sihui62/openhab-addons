<img src="/images/landroid.svg">

# WorxLandroid Binding

This is the binding for Worx Landroid robotic lawn mowers. It connects openHAB with your WorxLandroid Mower using the API and MQTT. This binding allows you to integrate, view and control supported Worx lawn mowers in the openHAB environment.

## Supported Things

Currently following Things are supported:

- **Bridge Worx Landroid API** Thing representing the handler for Worx API
- One or many Things for supported **Landroid Mower**'s

## Discovery

A Bridge is required to connect to the Worx API. Here you can provide your credentials for your WorxLandroid account. Once the Bridge has been added Worx Landroid Mowers will be discovered automatically.

## Binding Configuration

Following options can be set for the **Bridge Worx Landroid API**:

| Property          | Description                                                                                                                               |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| username          | Username to access the WorxLandroid API.                                                                                                  |
| password          | Password to access the WorxLandroid API.                                                                                                  |


Following options can be set for the **WorxLandroid Mower**:

| Property              | Description                                                                                            |
|-----------------------|--------------------------------------------------------------------------------------------------------|
| serialNumber          | Serial Number of the mower                                                                             |
| refreshStatusInterval | Interval for refreshing mower status (ONLINE/OFFLINE) and channel 'common#online' in seconds (min="30")|
| pollingInterval       | Interval for polling in seconds (min="30" max="7200").                                                 |


In order to prevent a 24h ban from worx, the following recommended settings seem to work:
| Property              | Value |
|-----------------------|-------|
| refreshStatusInterval |  1200 |
| pollingInterval       |  3600 |

Lower polling and refresh values will likely result in a 24h ban for your account.

## Channels

Currently following **Channels** are supported on the **Landroid Mower**:

##### common

| Channel   | Type | ChannelName | Values |
|------------|-----------|-----------|-----------|
| status      | `String` | common#status | |
| error      | `String` | common#error | |
| online      | `Switch` | common#online | |
| online-timestamp | `DateTime` | common#online-timestamp | |
| action | `String` | common#action | START, STOP, HOME |
| enable | `Switch` | common#enable | |
| lock | `Switch` | common#lock | |


##### config

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| timestamp  | `DateTime` | config#timestamp |
| command | `Number` | config#command |

##### multi-zones

If Multi Zones are supported, you are able to define 4 separate Zones and split working times by 10 to those.

To ease Zone Configuration, you are able to set distance in meters where a specific Zone starts. Bearing in mind that you roughly shall know how many meters of cable have been used (without buffer).

As second step you are able to set time in percent and split in parts of 10 between allocation zones.

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | multi-zones#enable |
| last-zone | `Number` | multi-zones#last-zone |
| zone-1 | `Number:Length` | multi-zones#zone-1 |
| zone-2 | `Number:Length` | multi-zones#zone-2 |
| zone-3 | `Number:Length` | multi-zones#zone-3 |
| zone-4 | `Number:Length` | multi-zones#zone-4 |
| allocation-0 | `Number` | multi-zones#allocation-0 | 
| allocation-1 | `Number` | multi-zones#allocation-1 |
| allocation-2 | `Number` | multi-zones#allocation-2 |
| allocation-3 | `Number` | multi-zones#allocation-3 |
| allocation-4 | `Number` | multi-zones#allocation-4 |
| allocation-5 | `Number` | multi-zones#allocation-5 |
| allocation-6 | `Number` | multi-zones#allocation-6 |
| allocation-7 | `Number` | multi-zones#allocation-7 |
| allocation-8 | `Number` | multi-zones#allocation-8 |
| allocation-9 | `Number` | multi-zones#allocation-9 |


##### schedule

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| mode | `String` | schedule#mode |
| time-extension | `Number` | schedule#time-extension |
| next-start | `DateTime` | schedule#next-start |
| next-stop | `DateTime` | schedule#next-stop |


##### aws

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| poll | `Switch` | aws#poll |
| connected | `Switch` | aws#connected |


##### sunday (Slot 1)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | sunday#enable |
| time | `DateTime` | sunday#time |
| duration | `Number:Time` | sunday#duration |
| edgecut | `Switch` | sunday#edgecut |


##### sunday2 (Slot 2, ONLY IF SUPPORTED)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | sunday2#enable |
| time | `DateTime` | sunday2#time |
| duration | `Number:Time` | sunday2#duration |
| edgecut | `Switch` | sunday2#edgecut |


##### monday (Slot 1)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | monday#enable |
| time | `DateTime` | monday#time |
| duration | `Number:Time` | monday#duration |
| edgecut | `Switch` | monday#edgecut |


##### monday2 (Slot 2, ONLY IF SUPPORTED)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | monday2#enable |
| time | `DateTime` | monday2#time |
| duration | `Number:Time` | monday2#duration |
| edgecut | `Switch` | monday2#edgecut |


##### tuesday (Slot 1)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | tuesday#enable |
| time | `DateTime` | tuesday#time |
| duration | `Number:Time` | tuesday#duration |
| edgecut | `Switch` | tuesday#edgecut |


##### tuesday2 (Slot 2, ONLY IF SUPPORTED)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | tuesday2#enable |
| time | `DateTime` | tuesday2#time |
| duration | `Number:Time` | tuesday2#duration |
| edgecut | `Switch` | tuesday2#edgecut |


##### wednesday (Slot 1)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | wednesday#enable |
| time | `DateTime` | wednesday#time |
| duration | `Number:Time` | wednesday#duration |
| edgecut | `Switch` | wednesday#edgecut |


##### wednesday2 (Slot 2, ONLY IF SUPPORTED)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | wednesday2#enable |
| time | `DateTime` | wednesday2#time |
| duration | `Number:Time` | wednesday2#duration |
| edgecut | `Switch` | wednesday2#edgecut |


##### thursday (Slot 1)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | thursday#enable |
| time | `DateTime` | thursday#time |
| duration | `Number:Time` | thursday#duration |
| edgecut | `Switch` | thursday#edgecut |


##### thursday2 (Slot 2, ONLY IF SUPPORTED)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | thursday2#enable |
| time | `DateTime` | thursday2#time |
| duration | `Number:Time` | thursday2#duration |
| edgecut | `Switch` | thursday2#edgecut |


##### friday (Slot 1)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | friday#enable |
| time | `DateTime` | friday#time |
| duration | `Number:Time` | friday#duration |
| edgecut | `Switch` | friday#edgecut |


##### friday2 (Slot 2, ONLY IF SUPPORTED)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | friday2#enable |
| time | `DateTime` | friday2#time |
| duration | `Number:Time` | friday2#duration |
| edgecut | `Switch` | friday2#edgecut |

##### saturday (Slot 1)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | saturday#enable |
| time | `DateTime` | saturday#time |
| duration | `Number:Time` | saturday#duration |
| edgecut | `Switch` | saturday#edgecut |


##### saturday2 (Slot 2, ONLY IF SUPPORTED)

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| enable | `Switch` | saturday2#enable |
| time | `DateTime` | saturday2#time |
| duration | `Number:Time` | saturday2#duration |
| edgecut | `Switch` | saturday2#edgecut |


##### one-time

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| edgecut | `Switch` | one-time#edgecut |
| duration | `Switch` | one-time#duration |


##### battery

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| temperature | `Number:Temperature` | battery#temperature |
| voltage | `Number:ElectricPotential` | battery#voltage |
| level | `Number` | battery#level |
| charge-cycles | `Number` | battery#charge-cycles |
| charge-cycles-total | `Number` | battery#charge-cycles-total |
| charging | `Switch` | battery#charging |


##### orientation

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| pitch | `Number:Angle` | orientation#pitch |
| roll | `Number:Angle` | orientation#roll |
| yaw | `Number:Angle` | orientation#yaw |


##### metrics

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| blade-time | `Number:Time` | metrics#blade-time |
| blade-time-total | `Number:Time` | metrics#blade-time-total |
| distance | `Number:Length` | metrics#distance |
| total-time | `Number:Time` | metrics#total-time |


##### rain

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| state | `Switch` | rain#state |
| counter | `Number:Time` | rain#counter |
| delay | `Number:Time` | rain#delay |


##### wifi

| Channel   | Type | ChannelName |
|------------|-----------|-----------|
| rssi | `Number:Power` | wifi#rssi |
| wifi-quality | `Number` | wifi#wifi-quality |


## Examples


### .items

```

String                     LandroidMowerCommonStatus               "Status code"               {channel="worxlandroid:mower:MyWorxBridge:mymower:common#status"}
String                     LandroidMowerCommonError                "Error code"                {channel="worxlandroid:mower:MyWorxBridge:mymower:common#error"}
Switch                     LandroidMowerCommonOnline               "Online"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:common#online"}
DateTime                   LandroidMowerCommonOnlineTimestamp      "Online status timestamp"   {channel="worxlandroid:mower:MyWorxBridge:mymower:common#online-timestamp"}
String                     LandroidMowerCommonAction               "Action"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:common#action"}
Switch                     LandroidMowerCommonEnable               "Mowing enabled"            {channel="worxlandroid:mower:MyWorxBridge:mymower:common#enable"}
Switch                     LandroidMowerCommonLock                 "Lock mower"                {channel="worxlandroid:mower:MyWorxBridge:mymower:common#lock"}


DateTime                   LandroidMowerConfigTimestamp            "Last update"               {channel="worxlandroid:mower:MyWorxBridge:mymower:config#timestamp"}
Number                     LandroidMowerConfigCommand              "Command"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:config#command"}



Switch                     LandroidMowerMultiZonesEnable           "Multizone enabled"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#enable"}
Number                     LandroidMowerMultiZonesLastZone         "Last zone"                 {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#last-zone"}
Number:Length              LandroidMowerMultiZonesZone1            "Meters zone 1"             {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#zone-1"}
Number:Length              LandroidMowerMultiZonesZone2            "Meters zone 2"             {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#zone-2"}
Number:Length              LandroidMowerMultiZonesZone3            "Meters zone 3"             {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#zone-3"}
Number:Length              LandroidMowerMultiZonesZone4            "Meters zone 4"             {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#zone-4"}
Number                     LandroidMowerMultiZonesAllocation0      "Zone allocation 1"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-0"}
Number                     LandroidMowerMultiZonesAllocation1      "Zone allocation 2"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-1"}
Number                     LandroidMowerMultiZonesAllocation2      "Zone allocation 3"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-2"}
Number                     LandroidMowerMultiZonesAllocation3      "Zone allocation 4"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-3"}
Number                     LandroidMowerMultiZonesAllocation4      "Zone allocation 5"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-4"}
Number                     LandroidMowerMultiZonesAllocation5      "Zone allocation 6"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-5"}
Number                     LandroidMowerMultiZonesAllocation6      "Zone allocation 7"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-6"}
Number                     LandroidMowerMultiZonesAllocation7      "Zone allocation 8"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-7"}
Number                     LandroidMowerMultiZonesAllocation8      "Zone allocation 9"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-8"}
Number                     LandroidMowerMultiZonesAllocation9      "Zone allocation 10"        {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-9"}



String                     LandroidMowerScheduleMode               "Schedule mode"             {channel="worxlandroid:mower:MyWorxBridge:mymower:schedule#mode"}
Number:Dimensionless       LandroidMowerScheduleTimeExtension      "Schedule time extension"   {channel="worxlandroid:mower:MyWorxBridge:mymower:schedule#time-extension"}
DateTime                   LandroidMowerScheduleNextStart          "Next start"                {channel="worxlandroid:mower:MyWorxBridge:mymower:schedule#next-start"}
DateTime                   LandroidMowerScheduleNextStop           "Next stop"                 {channel="worxlandroid:mower:MyWorxBridge:mymower:schedule#next-stop"}



Switch                     LandroidMowerAwsPoll                    "Poll aws"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:aws#poll"}
Switch                     LandroidMowerAwsConnected               "Connected"                 {channel="worxlandroid:mower:MyWorxBridge:mymower:aws#connected"}



Switch                     LandroidMowerSundayEnable               "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday#enable"}
DateTime                   LandroidMowerSundayTime                 "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday#time"}
Number:Time                LandroidMowerSundayDuration             "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday#duration"}
Switch                     LandroidMowerSundayEdgecut              "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday#edgecut"}
Switch                     LandroidMowerSunday2Enable              "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday2#enable"}
DateTime                   LandroidMowerSunday2Time                "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday2#time"}
Number:Time                LandroidMowerSunday2Duration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday2#duration"}
Switch                     LandroidMowerSunday2Edgecut             "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday2#edgecut"}
Switch                     LandroidMowerMondayEnable               "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:monday#enable"}
DateTime                   LandroidMowerMondayTime                 "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:monday#time"}
Number:Time                LandroidMowerMondayDuration             "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:monday#duration"}
Switch                     LandroidMowerMondayEdgecut              "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:monday#edgecut"}
Switch                     LandroidMowerMonday2Enable              "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:monday2#enable"}
DateTime                   LandroidMowerMonday2Time                "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:monday2#time"}
Number:Time                LandroidMowerMonday2Duration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:monday2#duration"}
Switch                     LandroidMowerMonday2Edgecut             "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:monday2#edgecut"}
Switch                     LandroidMowerTuesdayEnable              "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday#enable"}
DateTime                   LandroidMowerTuesdayTime                "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday#time"}
Number:Time                LandroidMowerTuesdayDuration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday#duration"}
Switch                     LandroidMowerTuesdayEdgecut             "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday#edgecut"}
Switch                     LandroidMowerTuesday2Enable             "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday2#enable"}
DateTime                   LandroidMowerTuesday2Time               "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday2#time"}
Number:Time                LandroidMowerTuesday2Duration           "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday2#duration"}
Switch                     LandroidMowerTuesday2Edgecut            "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday2#edgecut"}
Switch                     LandroidMowerWednesdayEnable            "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday#enable"}
DateTime                   LandroidMowerWednesdayTime              "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday#time"}
Number:Time                LandroidMowerWednesdayDuration          "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday#duration"}
Switch                     LandroidMowerWednesdayEdgecut           "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday#edgecut"}
Switch                     LandroidMowerWednesday2Enable           "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday2#enable"}
DateTime                   LandroidMowerWednesday2Time             "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday2#time"}
Number:Time                LandroidMowerWednesday2Duration         "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday2#duration"}
Switch                     LandroidMowerWednesday2Edgecut          "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday2#edgecut"}
Switch                     LandroidMowerThursdayEnable             "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday#enable"}
DateTime                   LandroidMowerThursdayTime               "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday#time"}
Number:Time                LandroidMowerThursdayDuration           "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday#duration"}
Switch                     LandroidMowerThursdayEdgecut            "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday#edgecut"}
Switch                     LandroidMowerThursday2Enable            "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday2#enable"}
DateTime                   LandroidMowerThursday2Time              "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday2#time"}
Number:Time                LandroidMowerThursday2Duration          "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday2#duration"}
Switch                     LandroidMowerThursday2Edgecut           "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday2#edgecut"}
Switch                     LandroidMowerFridayEnable               "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:friday#enable"}
DateTime                   LandroidMowerFridayTime                 "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:friday#time"}
Number:Time                LandroidMowerFridayDuration             "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:friday#duration"}
Switch                     LandroidMowerFridayEdgecut              "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:friday#edgecut"}
Switch                     LandroidMowerFriday2Enable              "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:friday2#enable"}
DateTime                   LandroidMowerFriday2Time                "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:friday2#time"}
Number:Time                LandroidMowerFriday2Duration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:friday2#duration"}
Switch                     LandroidMowerFriday2Edgecut             "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:friday2#edgecut"}
Switch                     LandroidMowerSaturdayEnable             "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday#enable"}
DateTime                   LandroidMowerSaturdayTime               "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday#time"}
Number:Time                LandroidMowerSaturdayDuration           "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday#duration"}
Switch                     LandroidMowerSaturdayEdgecut            "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday#edgecut"}
Switch                     LandroidMowerSaturday2Enable            "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday2#enable"}
DateTime                   LandroidMowerSaturday2Time              "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday2#time"}
Number:Time                LandroidMowerSaturday2Duration          "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday2#duration"}
Switch                     LandroidMowerSaturday2Edgecut           "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday2#edgecut"}


Switch                     LandroidMowerOneTimeEdgecut             "Schedule edgecut"          {channel="worxlandroid:mower:MyWorxBridge:mymower:one-time#edgecut"}
Number:Time                LandroidMowerOneTimeDuration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:one-time#duration"}



Number:Temperature         LandroidMowerBatteryTemperature         "Battery temperature"       {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#temperature"}
Number:ElectricPotential   LandroidMowerBatteryVoltage             "Battery voltage"           {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#voltage"}
Number                     LandroidMowerBatteryLevel               "Batterieladung"            {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#level"}
Number                     LandroidMowerBatteryChargeCycles        "Current charge cycles"     {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#charge-cycles"}
Number                     LandroidMowerBatteryChargeCyclesTotal   "Total charge cycles"       {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#charge-cycles-total"}
Switch                     LandroidMowerBatteryCharging            "Battery charging"          {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#charging"}




Number:Angle               LandroidMowerOrientationPitch           "Pitch"                     {channel="worxlandroid:mower:MyWorxBridge:mymower:orientation#pitch"}
Number:Angle               LandroidMowerOrientationRoll            "Roll"                      {channel="worxlandroid:mower:MyWorxBridge:mymower:orientation#roll"}
Number:Angle               LandroidMowerOrientationYaw             "Yaw"                       {channel="worxlandroid:mower:MyWorxBridge:mymower:orientation#yaw"}



Number:Time                LandroidMowerMetricsBladeTime           "Current blade time"        {channel="worxlandroid:mower:MyWorxBridge:mymower:metrics#blade-time"}
Number:Time                LandroidMowerMetricsBladeTimeTotal      "Total blade time"          {channel="worxlandroid:mower:MyWorxBridge:mymower:metrics#blade-time-total"}
Number:Length              LandroidMowerMetricsDistance            "Total distance"            {channel="worxlandroid:mower:MyWorxBridge:mymower:metrics#distance"}
Number:Time                LandroidMowerMetricsTotalTime           "Total time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:metrics#total-time"}



Switch                     LandroidMowerRainState                  "State"                     {channel="worxlandroid:mower:MyWorxBridge:mymower:rain#state"}
Number:Time                LandroidMowerRainCounter                "Counter"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:rain#counter"}
Number:Time                LandroidMowerRainDelay                  "Delay"                     {channel="worxlandroid:mower:MyWorxBridge:mymower:rain#delay"}




Number:Power               LandroidMowerWifiRssi                   "Rssi"                      {channel="worxlandroid:mower:MyWorxBridge:mymower:wifi#rssi"}
Number                     LandroidMowerWifiWifiQuality            "Signalstärke"              {channel="worxlandroid:mower:MyWorxBridge:mymower:wifi#wifi-quality"}
```
