OPENJTALKDICT="open_jtalk_dic_utf_8-1.11"

if [ ! -e ./cache/${OPENJTALKDICT} ]; then
  pushd ./cache
  wget https://github.com/r9y9/open_jtalk/releases/download/v1.11.1/${OPENJTALKDICT}.tar.gz
  tar xzf ${OPENJTALKDICT}.tar.gz
  popd
fi

if [ ! -e ./temp ]; then
  mkdir temp
fi
