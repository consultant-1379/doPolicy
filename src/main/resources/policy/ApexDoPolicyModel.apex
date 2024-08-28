model create name=ApexDoPolicyModel

schema create name=RuleSchemaType flavour=Avro schema=LS
#MACROFILE:"src/main/resources/schemas/RuleSchema.avsc"
LE

schema create name=HistoryContextSchema flavour=Avro schema=LS
#MACROFILE:"src/main/resources/schemas/HistoryContextSchema.avsc"
LE

schema create name=RulesArraySchemaType flavour=Avro schema=LS
#MACROFILE:"src/main/resources/schemas/RulesArrayTypeSchema.avsc"
LE

schema create name=RuleIdsArraySchemaType flavour=Avro schema=LS
#MACROFILE:"src/main/resources/schemas/RuleIdsArrayTypeSchema.avsc"
LE

schema create name=SimpleStringType  flavour=Java schema=java.lang.String
schema create name=SimpleIntegerType flavour=Java schema=java.lang.Integer
schema create name=SimpleArrayType flavour=Java schema=java.util.Arrays
schema create name=SimpleDateType flavour=Java schema=java.util.Date

schema create name=ThresholdSchemaType flavour=Avro schema=LS
#MACROFILE:"src/main/resources/schemas/ThresholdTypeSchema.avsc"
LE

schema create name=AffectedObjectsSchemaType flavour=Avro schema=LS
#MACROFILE:"src/main/resources/schemas/AffectedObjectsTypeSchema.avsc"
LE

schema create name=AffectedObjectsOutputSchemaType flavour=Avro schema=LS
#MACROFILE:"src/main/resources/schemas/AffectedObjectsOutputTypeSchema.avsc"
LE

album create name=ruleContext scope=policy writable=true schemaName=RuleSchemaType
album create name=historyContext scope=policy writable=true schemaName=HistoryContextSchema

event create name=RuleEvent nameSpace=org.onap.policy.apex.do.rule.engine source=RuleEngine target=APEX
event parameter create name=RuleEvent parName=rule          schemaName=RulesArraySchemaType

event create name=RuleOutEvent nameSpace=org.onap.policy.apex.do.rule.engine source=APEX target=logfile
event parameter create name=RuleOutEvent parName=id  schemaName=SimpleStringType

event create name=Trigger version=1.0 nameSpace=org.onap.policy.apex.ecm source=CENX target=APEX
event parameter create name=Trigger parName=name                      schemaName=SimpleStringType
event parameter create name=Trigger parName=nameSpace                 schemaName=SimpleStringType
event parameter create name=Trigger parName=version                   schemaName=SimpleStringType
event parameter create name=Trigger parName=source                    schemaName=SimpleStringType
event parameter create name=Trigger parName=target                    schemaName=SimpleStringType
event parameter create name=Trigger parName=eventTimeStamp            schemaName=SimpleStringType
event parameter create name=Trigger parName=eventType                 schemaName=SimpleStringType
event parameter create name=Trigger parName=faultAssetType            schemaName=SimpleStringType
event parameter create name=Trigger parName=faultAssetName            schemaName=SimpleStringType
event parameter create name=Trigger parName=faultAssetId              schemaName=SimpleStringType
event parameter create name=Trigger parName=severity                  schemaName=SimpleStringType
event parameter create name=Trigger parName=probableCause             schemaName=SimpleStringType
event parameter create name=Trigger parName=additionalText            schemaName=SimpleStringType
event parameter create name=Trigger parName=tenantName                schemaName=SimpleStringType
event parameter create name=Trigger parName=subtenantName             schemaName=SimpleStringType
event parameter create name=Trigger parName=vdcName                   schemaName=SimpleStringType
event parameter create name=Trigger parName=vdcId                     schemaName=SimpleStringType
event parameter create name=Trigger parName=threshold                 schemaName=ThresholdSchemaType
event parameter create name=Trigger parName=affectedObjects           schemaName=AffectedObjectsSchemaType

event create name=StateTrigger version=1.0 nameSpace=org.onap.policy.apex.ecm source=CENX target=APEX
event parameter create name=StateTrigger parName=name                      schemaName=SimpleStringType
event parameter create name=StateTrigger parName=nameSpace                 schemaName=SimpleStringType
event parameter create name=StateTrigger parName=version                   schemaName=SimpleStringType
event parameter create name=StateTrigger parName=source                    schemaName=SimpleStringType
event parameter create name=StateTrigger parName=target                    schemaName=SimpleStringType
event parameter create name=StateTrigger parName=eventTimeStamp            schemaName=SimpleStringType
event parameter create name=StateTrigger parName=eventType                 schemaName=SimpleStringType
event parameter create name=StateTrigger parName=faultAssetType            schemaName=SimpleStringType
event parameter create name=StateTrigger parName=faultAssetName            schemaName=SimpleStringType
event parameter create name=StateTrigger parName=faultAssetId              schemaName=SimpleStringType
event parameter create name=StateTrigger parName=severity                  schemaName=SimpleStringType
event parameter create name=StateTrigger parName=probableCause             schemaName=SimpleStringType
event parameter create name=StateTrigger parName=additionalText            schemaName=SimpleStringType
event parameter create name=StateTrigger parName=tenantName                schemaName=SimpleStringType
event parameter create name=StateTrigger parName=subtenantName             schemaName=SimpleStringType
event parameter create name=StateTrigger parName=vdcName                   schemaName=SimpleStringType
event parameter create name=StateTrigger parName=vdcId                     schemaName=SimpleStringType
event parameter create name=StateTrigger parName=threshold                 schemaName=ThresholdSchemaType
event parameter create name=StateTrigger parName=affectedObjects           schemaName=AffectedObjectsSchemaType
event parameter create name=StateTrigger parName=rules                     schemaName=RuleIdsArraySchemaType

event create name=Action nameSpace=org.onap.policy.apex.ecm source=APEX target=ECM
event parameter create name=Action parName=action                       schemaName=SimpleStringType
event parameter create name=Action parName=faultAssetType               schemaName=SimpleStringType
event parameter create name=Action parName=faultAssetName               schemaName=SimpleStringType
event parameter create name=Action parName=faultAssetId                 schemaName=SimpleStringType
event parameter create name=Action parName=vdcName                      schemaName=SimpleStringType
event parameter create name=Action parName=vdcId                        schemaName=SimpleStringType
event parameter create name=Action parName=affectedObjects              schemaName=AffectedObjectsOutputSchemaType
event parameter create name=Action parName=actionDateTime               schemaName=SimpleStringType
event parameter create name=Action parName=additionalText               schemaName=SimpleStringType
event parameter create name=Action parName=tenantName                   schemaName=SimpleStringType
event parameter create name=Action parName=subtenantName                schemaName=SimpleStringType


task create name=RegisterRuleToContextTask
task inputfield  create name=RegisterRuleToContextTask fieldName=rule   schemaName=RulesArraySchemaType

task outputfield create name=RegisterRuleToContextTask fieldName=id  schemaName=SimpleStringType

task contextref create name=RegisterRuleToContextTask albumName=ruleContext

task logic create name=RegisterRuleToContextTask logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/RegisterRuleToContextTask.js"
LE

task create name=RuleFilteringState1Task
task inputfield  create name=RuleFilteringState1Task fieldName=name                schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=nameSpace           schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=version             schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=source              schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=target              schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=eventTimeStamp      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=eventType           schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=faultAssetType      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=faultAssetName      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=faultAssetId        schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=severity            schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=additionalText      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=tenantName          schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=probableCause       schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=subtenantName       schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=vdcName             schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=vdcId               schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState1Task fieldName=threshold           schemaName=ThresholdSchemaType
task inputfield  create name=RuleFilteringState1Task fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType

task outputfield  create name=RuleFilteringState1Task fieldName=name                schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=nameSpace           schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=version             schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=source              schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=target              schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=eventTimeStamp      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=eventType           schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=faultAssetType      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=faultAssetName      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=faultAssetId        schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=severity            schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=additionalText      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=tenantName          schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=probableCause       schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=subtenantName       schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=vdcName             schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=vdcId               schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState1Task fieldName=threshold           schemaName=ThresholdSchemaType
task outputfield  create name=RuleFilteringState1Task fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task outputfield  create name=RuleFilteringState1Task fieldName=rules               schemaName=RuleIdsArraySchemaType

task contextref create name=RuleFilteringState1Task albumName=ruleContext

task logic create name=RuleFilteringState1Task logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/State1Task.js"
#MACROFILE:"src/main/resources/logic/Common.js"
LE

task create name=RuleFilteringState2Task
task inputfield  create name=RuleFilteringState2Task fieldName=name                schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=nameSpace           schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=version             schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=source              schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=target              schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=eventTimeStamp      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=eventType           schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=faultAssetType      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=faultAssetName      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=faultAssetId        schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=severity            schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=additionalText      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=tenantName          schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=probableCause       schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=subtenantName       schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=vdcName             schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=vdcId               schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState2Task fieldName=threshold           schemaName=ThresholdSchemaType
task inputfield  create name=RuleFilteringState2Task fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task inputfield  create name=RuleFilteringState2Task fieldName=rules               schemaName=RuleIdsArraySchemaType

task outputfield  create name=RuleFilteringState2Task fieldName=name                schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=nameSpace           schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=version             schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=source              schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=target              schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=eventTimeStamp      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=eventType           schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=faultAssetType      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=faultAssetName      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=faultAssetId        schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=severity            schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=additionalText      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=tenantName          schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=probableCause       schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=subtenantName       schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=vdcName             schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=vdcId               schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState2Task fieldName=threshold           schemaName=ThresholdSchemaType
task outputfield  create name=RuleFilteringState2Task fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task outputfield  create name=RuleFilteringState2Task fieldName=rules               schemaName=RuleIdsArraySchemaType

task contextref create name=RuleFilteringState2Task albumName=ruleContext

task logic create name=RuleFilteringState2Task logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/State2Task.js"
#MACROFILE:"src/main/resources/logic/Common.js"
LE

task create name=RuleFilteringState3Task
task inputfield  create name=RuleFilteringState3Task fieldName=name                schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=nameSpace           schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=version             schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=source              schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=target              schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=eventTimeStamp      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=eventType           schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=faultAssetType      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=faultAssetName      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=faultAssetId        schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=severity            schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=additionalText      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=tenantName          schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=probableCause       schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=subtenantName       schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=vdcName             schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=vdcId               schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState3Task fieldName=threshold           schemaName=ThresholdSchemaType
task inputfield  create name=RuleFilteringState3Task fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task inputfield  create name=RuleFilteringState3Task fieldName=rules               schemaName=RuleIdsArraySchemaType

task outputfield  create name=RuleFilteringState3Task fieldName=name                schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=nameSpace           schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=version             schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=source              schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=target              schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=eventTimeStamp      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=eventType           schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=faultAssetType      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=faultAssetName      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=faultAssetId        schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=severity            schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=additionalText      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=tenantName          schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=probableCause       schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=subtenantName       schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=vdcName             schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=vdcId               schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState3Task fieldName=threshold           schemaName=ThresholdSchemaType
task outputfield  create name=RuleFilteringState3Task fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task outputfield  create name=RuleFilteringState3Task fieldName=rules               schemaName=RuleIdsArraySchemaType

task contextref create name=RuleFilteringState3Task albumName=ruleContext

task logic create name=RuleFilteringState3Task logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/State3Task.js"
#MACROFILE:"src/main/resources/logic/Common.js"
LE

task create name=RuleFilteringState4Task
task inputfield  create name=RuleFilteringState4Task fieldName=name                schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=nameSpace           schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=version             schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=source              schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=target              schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=eventTimeStamp      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=eventType           schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=faultAssetType      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=faultAssetName      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=faultAssetId        schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=severity            schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=additionalText      schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=tenantName          schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=probableCause       schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=subtenantName       schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=vdcName             schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=vdcId               schemaName=SimpleStringType
task inputfield  create name=RuleFilteringState4Task fieldName=threshold           schemaName=ThresholdSchemaType
task inputfield  create name=RuleFilteringState4Task fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task inputfield  create name=RuleFilteringState4Task fieldName=rules               schemaName=RuleIdsArraySchemaType

task outputfield  create name=RuleFilteringState4Task fieldName=name                schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=nameSpace           schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=version             schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=source              schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=target              schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=eventTimeStamp      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=eventType           schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=faultAssetType      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=faultAssetName      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=faultAssetId        schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=severity            schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=additionalText      schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=tenantName          schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=probableCause       schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=subtenantName       schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=vdcName             schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=vdcId               schemaName=SimpleStringType
task outputfield  create name=RuleFilteringState4Task fieldName=threshold           schemaName=ThresholdSchemaType
task outputfield  create name=RuleFilteringState4Task fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task outputfield  create name=RuleFilteringState4Task fieldName=rules               schemaName=RuleIdsArraySchemaType

task contextref create name=RuleFilteringState4Task albumName=ruleContext

task logic create name=RuleFilteringState4Task logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/State4Task.js"
#MACROFILE:"src/main/resources/logic/Common.js"
LE


task create name=PrimaryActionTask
task inputfield  create name=PrimaryActionTask fieldName=name                schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=nameSpace           schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=version             schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=source              schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=target              schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=eventTimeStamp      schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=eventType           schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=faultAssetType      schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=faultAssetName      schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=faultAssetId        schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=severity            schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=additionalText      schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=tenantName          schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=probableCause       schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=subtenantName       schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=vdcName             schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=vdcId               schemaName=SimpleStringType
task inputfield  create name=PrimaryActionTask fieldName=threshold           schemaName=ThresholdSchemaType
task inputfield  create name=PrimaryActionTask fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task inputfield  create name=PrimaryActionTask fieldName=rules               schemaName=RuleIdsArraySchemaType

task outputfield create name=PrimaryActionTask fieldName=action            schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=faultAssetType    schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=faultAssetName    schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=faultAssetId      schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=vdcName           schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=vdcId             schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=affectedObjects   schemaName=AffectedObjectsOutputSchemaType
task outputfield create name=PrimaryActionTask fieldName=actionDateTime    schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=additionalText    schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=tenantName        schemaName=SimpleStringType
task outputfield create name=PrimaryActionTask fieldName=subtenantName     schemaName=SimpleStringType


task contextref create name=PrimaryActionTask albumName=ruleContext
task contextref create name=PrimaryActionTask albumName=historyContext

task logic create name=PrimaryActionTask logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/PrimaryActionTask.js"
#MACROFILE:"src/main/resources/logic/Common.js"
LE

task create name=AlternativeActionTask
task inputfield  create name=AlternativeActionTask fieldName=name                schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=nameSpace           schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=version             schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=source              schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=target              schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=eventTimeStamp      schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=eventType           schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=faultAssetType      schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=faultAssetName      schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=faultAssetId        schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=severity            schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=additionalText      schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=tenantName          schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=probableCause       schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=subtenantName       schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=vdcName             schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=vdcId               schemaName=SimpleStringType
task inputfield  create name=AlternativeActionTask fieldName=threshold           schemaName=ThresholdSchemaType
task inputfield  create name=AlternativeActionTask fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task inputfield  create name=AlternativeActionTask fieldName=rules               schemaName=RuleIdsArraySchemaType

task outputfield create name=AlternativeActionTask fieldName=action            schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=faultAssetType    schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=faultAssetName    schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=faultAssetId      schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=vdcName           schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=vdcId             schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=affectedObjects   schemaName=AffectedObjectsOutputSchemaType
task outputfield create name=AlternativeActionTask fieldName=actionDateTime    schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=additionalText    schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=tenantName        schemaName=SimpleStringType
task outputfield create name=AlternativeActionTask fieldName=subtenantName     schemaName=SimpleStringType


task contextref create name=AlternativeActionTask albumName=ruleContext
task contextref create name=AlternativeActionTask albumName=historyContext

task logic create name=AlternativeActionTask logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/AlternativeActionTask.js"
#MACROFILE:"src/main/resources/logic/Common.js"
LE

task create name=LogOnlyActionTask
task inputfield  create name=LogOnlyActionTask fieldName=name                schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=nameSpace           schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=version             schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=source              schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=target              schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=eventTimeStamp      schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=eventType           schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=faultAssetType      schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=faultAssetName      schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=faultAssetId        schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=severity            schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=additionalText      schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=tenantName          schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=probableCause       schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=subtenantName       schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=vdcName             schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=vdcId               schemaName=SimpleStringType
task inputfield  create name=LogOnlyActionTask fieldName=threshold           schemaName=ThresholdSchemaType
task inputfield  create name=LogOnlyActionTask fieldName=affectedObjects     schemaName=AffectedObjectsSchemaType
task inputfield  create name=LogOnlyActionTask fieldName=rules               schemaName=RuleIdsArraySchemaType

task outputfield create name=LogOnlyActionTask fieldName=action            schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=faultAssetType    schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=faultAssetName    schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=faultAssetId      schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=vdcName           schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=vdcId             schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=affectedObjects   schemaName=AffectedObjectsOutputSchemaType
task outputfield create name=LogOnlyActionTask fieldName=actionDateTime    schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=additionalText    schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=tenantName        schemaName=SimpleStringType
task outputfield create name=LogOnlyActionTask fieldName=subtenantName     schemaName=SimpleStringType


task contextref create name=LogOnlyActionTask albumName=ruleContext
task contextref create name=LogOnlyActionTask albumName=historyContext

task logic create name=LogOnlyActionTask logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/LogOnlyActionTask.js"
#MACROFILE:"src/main/resources/logic/Common.js"
LE

policy create name=RuleUpdatePolicy template=freestyle firstState=AddingRulestoMapState

policy state create name=RuleUpdatePolicy stateName=AddingRulestoMapState triggerName=RuleEvent defaultTaskName=RegisterRuleToContextTask
policy state output create name=RuleUpdatePolicy stateName=AddingRulestoMapState outputName=RuleUpdateStateOutput eventName=RuleOutEvent
policy state taskref create name=RuleUpdatePolicy stateName=AddingRulestoMapState taskName=RegisterRuleToContextTask outputType=DIRECT outputName=RuleUpdateStateOutput

policy create name=DecisionMakingPolicy template=freestyle firstState=RuleFilteringState1

policy state create name=DecisionMakingPolicy stateName=RuleFilteringState1 triggerName=Trigger defaultTaskName=RuleFilteringState1Task
policy state create name=DecisionMakingPolicy stateName=RuleFilteringState2 triggerName=StateTrigger defaultTaskName=RuleFilteringState2Task
policy state create name=DecisionMakingPolicy stateName=RuleFilteringState3 triggerName=StateTrigger defaultTaskName=RuleFilteringState3Task
policy state create name=DecisionMakingPolicy stateName=RuleFilteringState4 triggerName=StateTrigger defaultTaskName=RuleFilteringState4Task
policy state create name=DecisionMakingPolicy stateName=MatchAndDecisionState triggerName=StateTrigger defaultTaskName=PrimaryActionTask

policy state output create name=DecisionMakingPolicy stateName=RuleFilteringState1 outputName=DecisionStateOutput eventName=StateTrigger nextState=RuleFilteringState2
policy state taskref create name=DecisionMakingPolicy stateName=RuleFilteringState1 taskName=RuleFilteringState1Task outputType=DIRECT outputName=DecisionStateOutput
policy state contextref create name=DecisionMakingPolicy stateName=RuleFilteringState1 albumName=ruleContext


policy state output create name=DecisionMakingPolicy stateName=RuleFilteringState2 outputName=DecisionStateOutput eventName=StateTrigger nextState=RuleFilteringState3
policy state taskref create name=DecisionMakingPolicy stateName=RuleFilteringState2 taskName=RuleFilteringState2Task outputType=DIRECT outputName=DecisionStateOutput
policy state contextref create name=DecisionMakingPolicy stateName=RuleFilteringState2 albumName=ruleContext


policy state output create name=DecisionMakingPolicy stateName=RuleFilteringState3 outputName=DecisionStateOutput eventName=StateTrigger nextState=RuleFilteringState4
policy state taskref create name=DecisionMakingPolicy stateName=RuleFilteringState3 taskName=RuleFilteringState3Task outputType=DIRECT outputName=DecisionStateOutput
policy state contextref create name=DecisionMakingPolicy stateName=RuleFilteringState3 albumName=ruleContext


policy state output create name=DecisionMakingPolicy stateName=RuleFilteringState4 outputName=DecisionStateOutput eventName=StateTrigger nextState=MatchAndDecisionState
policy state taskref create name=DecisionMakingPolicy stateName=RuleFilteringState4 taskName=RuleFilteringState4Task outputType=DIRECT outputName=DecisionStateOutput
policy state contextref create name=DecisionMakingPolicy stateName=RuleFilteringState4 albumName=ruleContext


policy state output create name=DecisionMakingPolicy stateName=MatchAndDecisionState outputName=DecisionStateOutput eventName=Action
policy state taskref create name=DecisionMakingPolicy stateName=MatchAndDecisionState taskName=PrimaryActionTask outputType=DIRECT outputName=DecisionStateOutput
policy state taskref create name=DecisionMakingPolicy stateName=MatchAndDecisionState taskName=AlternativeActionTask outputType=DIRECT outputName=DecisionStateOutput
policy state taskref create name=DecisionMakingPolicy stateName=MatchAndDecisionState taskName=LogOnlyActionTask outputType=DIRECT outputName=DecisionStateOutput
policy state contextref create name=DecisionMakingPolicy stateName=MatchAndDecisionState albumName=ruleContext
policy state contextref create name=DecisionMakingPolicy stateName=MatchAndDecisionState albumName=historyContext
policy state selecttasklogic create name=DecisionMakingPolicy stateName=MatchAndDecisionState logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"src/main/resources/logic/ActionSelectionTask.js"
#MACROFILE:"src/main/resources/logic/Common.js"
LE

validate