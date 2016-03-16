# Hypermedia Controls for the Internet of Things

## Quick Demo Setup

1. Start RD (entry point) and "emulated Web" (linked resources)

       ./demos/start_location_descriptions.sh

2. Start [cf-polyfill](https://github.com/eclipse/californium.tools/tree/master/cf-polyfill) and open [CoRE-HAL Explorer](https://github.com/mkovatsc/core-hal-explorer) and look at linked nodes

       ./demos/start_cf_polyfill.sh

   [CoRE-HAL Explorer](http://mkovatsc.github.io/core-hal-explorer/)

3. Start lightbulb A ("candle") and re-run CoRE-HAL Explorer to see new resources.

       ./demos/start_candle.sh

4. Start `ch.ethz.inf.vs.hypermedia.corehal.server.TestLightBulb` and watch lightbulb A, which should start changning color
5. Kill lightbulb A
6. Start lightbulb B, which should start changing color, since the TestLightBulb Hypermedia Client re-discovered the replaced device

       ./demos/start_lightbulb.sh

7. Re-run CoRE-HAL Explorer and see that lightbulb B has different endpoint address (port number) and different resource structure
