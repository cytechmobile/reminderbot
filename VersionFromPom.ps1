[xml]$pomXml = Get-Content .\pom.xml
# version
Write-Host $pomXml.project.version
$version=$pomXml.project.version
Write-Host "##vso[task.setvariable variable=version]$version"