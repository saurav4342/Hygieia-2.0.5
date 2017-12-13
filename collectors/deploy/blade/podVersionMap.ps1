
cls ### clear screen

##Write-Host "PODNAME  ----- VERSION ----- DEPLOYED ON"
##Write-Host "======================================================"

foreach($line in Get-Content C:\Users\nayaksau\Documents\script\file.txt) {  ## looping SERVER-POD mapping file 
$server,$pod=$line.split("-");  ## separating server and pod and assigning to variable 
$pod=$pod -replace "`n|`r" ## removing new-line character in the $pod variable
if($pod.Contains("AP")){
$fileName = get-ChildItem -path \\$server\d$\AdminPortal "BinaryManifest*"
$binary,$app,$version = $fileName.Name.Split("_")
$versionnumber= $version.Replace(".xml","")
}
elseif($pod.Contains("DB") -or $pod.Contains("_")){
$paths = "\\Dc2eucfs1\dc2euct2\shares\TLMC\TLMCSMITNAS1\CfgMgmt\TFSDrops\DeploymentLogs\14.0.00\ClientDB","\\Dc2eucfs1\dc2euct2\shares\TLMC\TLMCSMITNAS1\CfgMgmt\TFSDrops\DeploymentLogs\14.0.00\SystemDB","\\dc2eucfs1\dc2euct2\shares\TLMC\TLMCSMITNAS1\CfgMgmt\TFSDrops\DeploymentLogs\15.0.00\ClientDB"
forEach($path in $paths){
$files = Get-ChildItem -path $path | Sort {$_.LastWriteTime} -Descending
$currentDate= Get-Date
forEach($file in $files){
#echo $file.LastWriteTime + " " + $currentDate.Date
if($file.Name.Contains($pod)){
$finalName = $file.Name.Replace("Logs_","").Replace(".zip","") -match "\d{2}\.\d{2}\.\d{2}\.\d{1,7}"
#echo $finalName
if( $finalName){
$versionnumber = $matches[0]
}
break
}
}
}
}
else{
 $webConfig = get-content -path \\$server\d$\ADP\ezLM\net2\ezLaborManagerNet\web.config ## getting web.config from each server and assigning to a variable 
 $webConfigXml = New-Object XML  ## creating an XML object
 $webConfigXml.LoadXml($webConfig) ## load the text file content into XML object

$version=$webConfigXml.configuration.appSettings.add | where {$_.Key -eq 'JSCacheValue'} ## getting JSCacheValue as key value pair
$date=$webConfigXml.configuration.appSettings.add | where {$_.Key -eq 'AppReleaseDate'}  ## getting AppReleaseDate as key value pair
                           
$versionnumber=$version.value  ## capturing value of key JSCacheValue
$AppReleaseDate=$date.value    ## capturing value of key AppReleaseDate
}
Write-Host "$pod  --- $versionnumber --- $AppReleaseDate" ### display the versions

}
