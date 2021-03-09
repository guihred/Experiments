import idautils
import idaapi
import idc

for name in idautils.Names():
    if name[1] == "dec_function":
        ea= idc.get_name_ea_simple("dec_function")
        for ref in idautils.CodeRefsTo(ea, 1):
            idc.add_bpt(ref)

idc.start_process('', '', '')
while True:
    event_code = idc.wait_for_next_event(idc.WFNE_SUSP, -1)
    if event_code < 1 or event_code == idc.PROCESS_EXITED:
        break
    rcx_value = idc.get_reg_value("RCX")
    encoded_string = idc.get_strlit_contents(rcx_value)
    idc.step_over()
    evt_code = idc.wait_for_next_event(idc.WFNE_SUSP, -1)
    if evt_code == idc.BREAKPOINT:
        rax_value = idc.get_reg_value("RAX")

    decoded_string = idc.get_strlit_contents(rax_value)
    print("{0} {1:>25}".format(encoded_string, decoded_string))
    idc.resume_process()
