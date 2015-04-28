$release_version = $args[0]
$tag ="v"+$release_version

write-output "===== Checking out tag ====="
git checkout $tag
git clean -fdx


write-output "===== Building App ===="
mvn -DskipTests -projects application --also-make clean install
write-output "===== Building Installer ===="
mvn -Pdeployment -Pdeploy-windows --projects application install

write-output "===== DONE ====="
