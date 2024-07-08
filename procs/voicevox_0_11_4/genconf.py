
for i in range(51):
  print(f"""
  - serviceId: VoiceVox_0_14_2_{i:02}|R
    implementation: org.langrid.mlgridservices.service.impl.CmdReplTextToSpeech
    properties:
      commands:
        - bash
        - run_repl.sh
        - {i}
""")
