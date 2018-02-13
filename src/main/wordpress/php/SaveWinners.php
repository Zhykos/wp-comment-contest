<?php
/*  Copyright 2009 - 2018 Comment Contest plug-in for Wordpress by Thomas "Zhykos" Cicognani  (email : zhykos@gmail.com)

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

require_once dirname(__FILE__) . '/../../../../wp-config.php';
require_once ABSPATH . 'wp-admin/includes/plugin.php';

if (current_user_can(10)) {
    $winnersStr = strip_tags($_POST['winners']);
	$winners = explode(",", $winnersStr);
	$postID = intval($_POST['post']);

	delete_post_meta($postID, "wp-comment-contest-winner");
	foreach($winners as $winner) {
		add_post_meta($postID, "wp-comment-contest-winner", intval($winner), false);
	}
}