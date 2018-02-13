<?php
/*  Copyright (c) 2009, 2018 Comment Contest plug-in for WordPress by Thomas Cicognani

 Contributors:
    Thomas Cicognani - Full renaming with 3.0 + check compatibility with WordPress 4.9.4

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

header("Content-type: text/plain");
header("Content-Disposition: attachment; filename=wp_comment_contest-report.txt");

require_once dirname(__FILE__) . '/../../../../wp-config.php';
require_once ABSPATH . 'wp-admin/includes/plugin.php';

if (current_user_can(10)) {

    global $wpdb;

    echo "WordPress:\n";
    echo "\t- Version " . get_bloginfo("version");

    echo "\n\n";

    echo "Server:\n";
    echo "\t- PHP " . phpversion() . "\n";
    echo "\t- MySQL " . $wpdb->db_version();

    echo "\n\n";

    echo "Plugins:\n";
    $all_plugins = get_plugins();
    foreach($all_plugins as $id => $info) {
        $isActive = is_plugin_active($id);
        echo sprintf("\t- %s\t/\t%s\t/\t%s\t/\t%s\n",
                $info["Name"], $info["Version"], $info["PluginURI"], ($isActive ? "ACTIVE" : "INSTALLED"));
    }
}