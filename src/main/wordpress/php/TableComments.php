<?php
/*  Copyright 2009 - 2014 Comment Contest plug-in for Wordpress by Thomas "Zhykos" Cicognani  (email : zhykos@gmail.com)

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

require_once("AbstractTable.php");

/**
 * Table with comments which are used for contest
 */
class TableComments extends AbstractTable {

    public function __construct($postID) {
        parent::__construct($postID);
    }

    protected function displayTBodyStart() {
        echo '<tbody id="the-list-contest">
            <tr class="no-items" style="display: none" id="comment-contest-not-found-tr"><td class="colspanchange" colspan="' . $this->get_column_count() . '">';
    }

    protected function displayTrTable($commentID) {
        echo "<tr id='comment-contest-$commentID'>";
    }

    protected function getActions($commentID) {
         $actions = array(
            'delete' => "<a href='javascript:;' onclick='commentContestDelete($commentID)' id='deleteLink-$commentID' class='delete'>" . __('Delete', "fr.zhykos.wordpress.commentcontest") . '</a>',
            'restore' => "<a href='javascript:;' onclick='commentContestRestore($commentID)' id='restoreLink-$commentID' class='restoreLink'>" . __('Restore', "fr.zhykos.wordpress.commentcontest") . '</a>',
            'cheat' => "<a href='javascript:;' onclick='commentContestCheat($commentID)' id='cheatLink-$commentID' class='cheatLink'>" . __('Cheat', "fr.zhykos.wordpress.commentcontest") . '</a>',
            'stopcheat' => "<a href='javascript:;' onclick='commentContestStopCheat($commentID)' id='stopCheatLink-$commentID' class='stopCheatLink'>" . __('Stop cheating', "fr.zhykos.wordpress.commentcontest") . '</a>');
         return $actions;
    }
    
    public function get_bulk_actions() {
	$actions = array(
            'delete' => __('Delete', "fr.zhykos.wordpress.commentcontest"),
            'restore' => __('Restore', "fr.zhykos.wordpress.commentcontest"));
        return $actions;
    }

    protected function getViewFunction($roleID) {
        return "selectRole(\"$roleID\")";
    }
}
?>
