# Simple script for executing application with different parameters
max=16
for i in `seq 2 $max`
do
    echo "java -jar ./app/build/libs/app.jar 10 $i 2000 TWO"
    java -jar ./app/build/libs/app.jar 10 $i 2000 TWO
done