# InstantNetherPortals - Build Script
# 1. Reads the current version from pom.xml
# 2. Runs mvn clean package
# 3. Copies the JAR to versions/ as  [name]_[v#.#]_[mc-version].jar
# 4. Commits the new JAR + pom.xml bump, tags the commit, pushes to GitHub
# 5. Creates a GitHub release and attaches the JAR as an asset
# 6. Bumps the minor version in pom.xml for the next build

$PomPath    = Join-Path $PSScriptRoot "pom.xml"
$TargetDir  = Join-Path $PSScriptRoot "target"
$VersionDir = Join-Path $PSScriptRoot "versions"

# Read current version and mc.version from pom.xml
$pomContent = Get-Content $PomPath -Raw -Encoding UTF8

if ($pomContent -notmatch '(?s)<artifactId>InstantNetherPortals</artifactId>\s*<version>([^<]+)</version>') {
    Write-Host "ERROR: Could not read project version from pom.xml" -ForegroundColor Red
    exit 1
}
$currentVersion = $Matches[1].Trim()

if ($pomContent -notmatch '<mc\.version>([^<]+)</mc\.version>') {
    Write-Host "ERROR: Could not read mc.version from pom.xml" -ForegroundColor Red
    exit 1
}
$mcVersion = $Matches[1].Trim()

Write-Host ""
Write-Host "  Building InstantNetherPortals v$currentVersion for MC $mcVersion" -ForegroundColor Cyan
Write-Host ""

# Prefer the newer JDK
$newerJdk = "C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot"
if (Test-Path $newerJdk) {
    $env:JAVA_HOME = $newerJdk
}

# Maven build
Push-Location $PSScriptRoot
mvn clean package --no-transfer-progress
$buildResult = $LASTEXITCODE
Pop-Location

if ($buildResult -ne 0) {
    Write-Host ""
    Write-Host "  BUILD FAILED - version NOT incremented, no release created." -ForegroundColor Red
    exit 1
}

# Copy to versions/
if (-not (Test-Path $VersionDir)) {
    New-Item -ItemType Directory -Path $VersionDir | Out-Null
}

$sourceJar  = Join-Path $TargetDir "InstantNetherPortals-${currentVersion}.jar"
$outputName = "InstantNetherPortals_v${currentVersion}_${mcVersion}.jar"
$outputJar  = Join-Path $VersionDir $outputName

if (-not (Test-Path $sourceJar)) {
    Write-Host "  ERROR: Built JAR not found at $sourceJar" -ForegroundColor Red
    exit 1
}

Copy-Item $sourceJar $outputJar -Force
Write-Host ""
Write-Host "  Output: versions/$outputName" -ForegroundColor Green

# Increment minor version in pom.xml
$parts = $currentVersion -split "\."
$major = [int]$parts[0]
$minor = [int]$parts[1]
$newVersion = "$major.$($minor + 1)"

$pattern     = '(?s)(<artifactId>InstantNetherPortals</artifactId>\s*<version>)[^<]+(</version>)'
$replacement = '${1}' + $newVersion + '${2}'
$updatedContent = $pomContent -replace $pattern, $replacement
Set-Content -Path $PomPath -Value $updatedContent -Encoding UTF8 -NoNewline

Write-Host "  pom.xml version bumped:  $currentVersion  ->  $newVersion" -ForegroundColor Yellow

# Git commit + tag
$tag = "v$currentVersion"
git add versions/$outputName pom.xml
git commit -m "Release $tag for MC $mcVersion"
git tag $tag
git push
git push --tags

# GitHub release
$releaseTitle = "v$currentVersion for MC $mcVersion"
$releaseNotes = "InstantNetherPortals $tag - Paper $mcVersion"
gh release create $tag $outputJar --title $releaseTitle --notes $releaseNotes

Write-Host ""
Write-Host "  GitHub release created: $tag" -ForegroundColor Green
Write-Host ""
