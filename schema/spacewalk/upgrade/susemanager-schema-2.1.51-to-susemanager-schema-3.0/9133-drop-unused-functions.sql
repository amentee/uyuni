
drop function if exists rhn_channel.available_chan_subscriptions(channel_id_in IN NUMERIC, org_id_in IN NUMERIC);
drop function if exists rhn_channel.available_family_subscriptions(channel_family_id_in IN NUMERIC, org_id_in IN NUMERIC);
drop function if exists rhn_channel.available_fve_chan_subs(channel_id_in IN NUMERIC, org_id_in IN NUMERIC);
drop function if exists rhn_channel.available_fve_family_subs(channel_family_id_in IN NUMERIC, org_id_in IN NUMERIC);
drop function if exists rhn_channel.can_convert_to_fve(server_id_in IN NUMERIC, channel_family_id_val IN NUMERIC);
drop function if exists rhn_channel.can_server_consume_fve(server_id_in in numeric);
drop function if exists rhn_channel.can_server_consume_virt_channl(server_id_in in numeric, family_id_in in numeric );
drop function if exists rhn_channel.cfam_curr_fve_members(channel_family_id_in IN NUMERIC, org_id_in IN NUMERIC);
drop function if exists rhn_channel.channel_family_current_members(channel_family_id_in IN NUMERIC, org_id_in IN NUMERIC);
drop function if exists rhn_channel.clear_subscriptions(server_id_in numeric, deleting_server numeric, update_family_countsyn numeric);
drop function if exists rhn_channel.convert_to_fve(server_id_in IN NUMERIC, channel_family_id_val IN NUMERIC);
drop function if exists rhn_channel.delete_server_channels(server_id_in in numeric);
drop function if exists rhn_channel.server_has_family_subscription(server_id_in DECIMAL, channel_family_id_in DECIMAL);
drop function if exists rhn_channel.subscribe_server(server_id_in numeric, channel_id_in numeric, immediate_in numeric, user_id_in numeric, recalcfamily_in numeric);
drop function if exists rhn_channel.unsubscribe_server(server_id_in numeric, channel_id_in numeric, immediate_in numeric, unsubscribe_children_in numeric, deleting_server numeric, update_family_countsyn numeric);
drop function if exists rhn_channel.update_family_counts(channel_family_id_in IN NUMERIC, org_id_in IN NUMERIC);
drop function if exists rhn_channel.update_group_family_counts(group_label_in IN VARCHAR, org_id_in IN NUMERIC);

drop function if exists rhn_entitlements.activate_channel_entitlement(org_id_in in numeric, channel_family_label_in in varchar, quantity_in in numeric, flex_in in numeric);
drop function if exists rhn_entitlements.activate_system_entitlement(org_id_in numeric, group_label_in character varying, quantity_in numeric);
drop function if exists rhn_entitlements.assign_channel_entitlement(channel_family_label_in in varchar, from_org_id_in in numeric, to_org_id_in in numeric, quantity_in in numeric, flex_in in numeric);
drop function if exists rhn_entitlements.assign_system_entitlement(group_label_in character varying, from_org_id_in numeric, to_org_id_in numeric, quantity_in numeric);
drop function if exists rhn_entitlements.create_entitlement_group(org_id_in numeric, type_label_in character varying);
drop function if exists rhn_entitlements.entitle_last_modified_servers(customer_id_in numeric, type_label_in character varying, quantity_in numeric);
drop function if exists rhn_entitlements.lookup_entitlement_group(org_id_in numeric, type_label_in character varying);
drop function if exists rhn_entitlements.modify_org_service(org_id_in numeric, service_label_in character varying, enable_in character);
drop function if exists rhn_entitlements.prune_family(customer_id_in in numeric, channel_family_id_in in numeric, quantity_in in numeric, flex_in in numeric);
drop function if exists rhn_entitlements.prune_group(group_id_in in numeric, quantity_in in numeric, update_family_countsYN in numeric);
drop function if exists rhn_entitlements.prune_group(group_id_in numeric, quantity_in numeric);
drop function if exists rhn_entitlements.remove_org_entitlements(org_id_in numeric);
drop function if exists rhn_entitlements.remove_server_entitlement(server_id_in numeric, type_label_in character varying, repoll_virt_guests numeric);
drop function if exists rhn_entitlements.repoll_virt_guest_entitlements(server_id_in numeric);
drop function if exists rhn_entitlements.set_customer_enterprise(customer_id_in numeric);
drop function if exists rhn_entitlements.set_customer_monitoring(customer_id_in in numeric);
drop function if exists rhn_entitlements.set_customer_nonlinux(customer_id_in numeric);
drop function if exists rhn_entitlements.set_customer_provisioning(customer_id_in in numeric);
drop function if exists rhn_entitlements.set_family_count(customer_id_in in numeric, channel_family_id_in in numeric, quantity_in in numeric, flex_in in numeric);
drop function if exists rhn_entitlements.set_server_group_count(customer_id_in in numeric, group_type_in in numeric, quantity_in in numeric, update_family_countsYN in numeric);
drop function if exists rhn_entitlements.set_server_group_count(customer_id_in numeric, group_type_in numeric, quantity_in numeric);
drop function if exists rhn_entitlements.subscribe_newest_servers(customer_id_in in numeric);
drop function if exists rhn_entitlements.unset_customer_enterprise(customer_id_in numeric);
drop function if exists rhn_entitlements.unset_customer_monitoring(customer_id_in in numeric);
drop function if exists rhn_entitlements.unset_customer_nonlinux(customer_id_in numeric);
drop function if exists rhn_entitlements.unset_customer_provisioning(customer_id_in in numeric);

drop function if exists rhn_cmeth_val_trig_fun();
drop function if exists rhn_org_ent_mod_trig_fun();
drop function if exists rhn_org_ent_type_mod_trig_fun();
drop function if exists rhn_probe_delete_trigger_fun();
drop function if exists rhn_probe_insert_trigger_fun();
drop function if exists rhn_satcert_mod_trig_fun();
drop function if exists rhn_servnet_ipaddr_mon_trig_fun();
drop function if exists rhn_solaris_p_mod_trig_fun();
drop function if exists rhn_solaris_ps_mod_trig_fun();
drop function if exists rhn_solaris_psm_mod_trig_fun();
drop function if exists time_series_purge_mod_trig_fun();