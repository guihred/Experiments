Set oShell = CreateObject ("Wscript.Shell") 
Dim strArgs
strArgs = "mvn exec:java -Dexec.mainClass=graphs.app.AllApps"
oShell.Run strArgs, 0, false