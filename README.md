GR8Conf Europe Agenda Application
---------------------------------

[![Build Status](https://travis-ci.org/melix/gr8confagenda.svg?branch=master)](https://travis-ci.org/melix/gr8confagenda)

This is the source code for the GR8Conf agenda application as found on Google Play Store, and demonstrated during GR8Conf Europe 2014.

This application is fully written in [Groovy](http://groovy.codehaus.org).

Agenda for a specific conference
---
TO load a specific conference event two changes must be made:

`me.champeau.gr8confagenda.app.SessionListActivity.BASE_DATE` must be set to the start date of the event
`me.champeau.gr8confagenda.app.client.AgendaClient.CONFERENCE_ID` must be set to the conference ID (1: GR8Conf EU 2014, 2: GR8Conf US 2014, 3: GR8Conf EU 2015, 4: GR8Conf US 2015, etc...)

All this should be configurable in the future, for now this is what we live with.

License
---

This application licensed under the terms of the [Apache License, Version 2.0][Apache License, Version 2.0].
[Apache License, Version 2.0]: http://www.apache.org/licenses/LICENSE-2.0.html

