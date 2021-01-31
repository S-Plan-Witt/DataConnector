#
# Copyright (c) 2021. Nils Witt
#

jpackage --input target/ \
  --dest build/ \
  --name S-Plan_DataConnector \
  --main-jar S-Plan_DataConnector-jar-with-dependencies.jar \
  --main-class de.nilswitt.splan.Main \
  --mac-sign \
  --type dmg