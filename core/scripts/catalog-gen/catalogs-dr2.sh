#!/usr/bin/env bash

# This script runs the generation of all LOD DR2 catalogs

GS_LOC="$HOME/gaiasky"
LOGS_LOC="$GS_LOC/dr2_logs"
DATA_LOC="$HOME/gaiadata"
DR_BASE="$DATA_LOC/DR2"
DR_LOC="$DR_LOC/dr2"

# Add here the names of the datasets to generate
# Values: small, default, bright, large, verylarge, extralarge, ratherlarge, ruwe
TORUN=("small" "default")

function contains() {
    local n=$#
    local value=${!n}
    for ((i=1;i < $#;i++)) {
        if [ "${!i}" == "${value}" ]; then
            echo "y"
            return 0
        fi
    }
    echo "n"
    return 1
}

# SMALL
if [ $(contains "${A[@]}" "small") == "y" ]; then
  DSNAME="001_$(date +'%Y%m%d')_dr2-small"
  nohup ./octreegen --loader CsvCatalogDataProvider --input $DR_LOC/csv/ --output $DR_LOC/out/$DSNAME/ --maxpart 100000 --pllxerrbright 0.1 --pllxerrfaint 0.005 --hip --pllxzeropoint -0.029 --magcorrections --xmatchfile $GS_LOC/data/gdr2hip/gdr2-hip-xmatch-all.csv > $LOGS_LOC/$DSNAME.out
fi

# DEFAULT
if [ $(contains "${A[@]}" "default") == "y" ]; then
  DSNAME="002_$(date +'%Y%m%d')_dr2-default"
  nohup ./octreegen --loader CsvCatalogDataProvider --input $DR_LOC/csv/ --output $DR_LOC/out/$DSNAME/ --maxpart 100000 --pllxerrbright 0.2 --pllxerrfaint 0.005 --hip --pllxzeropoint -0.029 --magcorrections --xmatchfile $GS_LOC/data/gdr2hip/gdr2-hip-xmatch-all.csv > $LOGS_LOC/$DSNAME.out
fi

# BRIGHT
if [ $(contains "${A[@]}" "bright") == "y" ]; then
  DSNAME="003_$(date +'%Y%m%d')_dr2-bright"
  nohup ./octreegen --loader CsvCatalogDataProvider --input $DR_LOC/csv/ --output $DR_LOC/out/$DSNAME/ --maxpart 100000 --pllxerrbright 0.9 --pllxerrfaint 0.0 --hip --pllxzeropoint -0.029 --magcorrections --xmatchfile $GS_LOC/data/gdr2hip/gdr2-hip-xmatch-all.csv > $LOGS_LOC/$DSNAME.out
fi

# LARGE
if [ $(contains "${A[@]}" "large") == "y" ]; then
  DSNAME="004_$(date +'%Y%m%d')_dr2-large"
  nohup ./octreegen --loader CsvCatalogDataProvider --input $DR_LOC/csv/ --output $DR_LOC/out/$DSNAME/ --maxpart 100000 --pllxerrbright 0.5 --pllxerrfaint 0.125 --hip --pllxzeropoint -0.029 --magcorrections --xmatchfile $GS_LOC/data/gdr2hip/gdr2-hip-xmatch-all.csv > $LOGS_LOC/$DSNAME.out
fi

# VERYLARGE
if [ $(contains "${A[@]}" "verylarge") == "y" ]; then
  DSNAME="005_$(date +'%Y%m%d')_dr2-verylarge"
  nohup ./octreegen --loader CsvCatalogDataProvider --input $DR_LOC/csv/ --output $DR_LOC/out/$DSNAME/ --maxpart 100000 --pllxerrbright 0.9 --pllxerrfaint 0.9 --hip --pllxzeropoint -0.029 --postprocess --childcount 10000 --parentcount 50000 --magcorrections --xmatchfile $GS_LOC/data/gdr2hip/gdr2-hip-xmatch-all.csv > $LOGS_LOC/$DSNAME.out
fi

# EXTRALARGE
if [ $(contains "${A[@]}" "extralarge") == "y" ]; then
  DSNAME="006_$(date +'%Y%m%d')_dr2-extralarge"
  nohup ./octreegen --loader CsvCatalogDataProvider --input $DR_LOC/csv/ --output $DR_LOC/out/$DSNAME/ --maxpart 100000 --hip --pllxzeropoint -0.029 --geodistfile $DR_BASE/geo_distances/ --postprocess --childcount 10000 --parentcount 50000 --magcorrections --xmatchfile $GS_LOC/data/gdr2hip/gdr2-hip-xmatch-all.csv > $LOGS_LOC/$DSNAME.out
fi

# RATHERLARGE
if [ $(contains "${A[@]}" "ratherlarge") == "y" ]; then
  DSNAME="007_$(date +'%Y%m%d')_dr2-ratherlarge"
  nohup ./octreegen --loader CsvCatalogDataProvider --input $DR_LOC/csv/ --output $DR_LOC/out/$DSNAME/ --maxpart 100000 --pllxerrbright 0.5 --pllxerrfaint 0.5 --hip --pllxzeropoint -0.029 --postprocess --childcount 1000 --parentcount 50000 --magcorrections --xmatchfile $GS_LOC/data/gdr2hip/gdr2-hip-xmatch-all.csv > $LOGS_LOC/$DSNAME.out
fi

# RUWE
if [ $(contains "${A[@]}" "ruwe") == "y" ]; then
  DSNAME="008_$(date +'%Y%m%d')_dr2-ruwe"
  nohup ./octreegen --loader CsvCatalogDataProvider --input $DR_LOC/csv/ --output $DR_LOC/out/$DSNAME/ --maxpart 100000 --ruwe 1.4 --ruwe-file $DR_BASE/ruwe/ruwes.txt.gz --hip --pllxzeropoint -0.029 --postprocess --childcount 1000 --parentcount 50000 --magcorrections --xmatchfile $GS_LOC/data/gdr2hip/gdr2-hip-xmatch-all.csv > $LOGS_LOC/$DSNAME.out
fi