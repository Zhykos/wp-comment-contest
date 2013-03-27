<?php
/*  Copyright 2009 - 2013 Comment Contest plug-in for Wordpress by Thomas "Zhykos" Cicognani  (email : tcicognani@zhyweb.org)

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

require_once("OrgZhyweb_WPCommentContest_AbstractTableUI.php");

/**
 * Table with comments which are used for contest
 */
class OrgZhyweb_WPCommentContest_TableUI extends OrgZhyweb_WPCommentContest_AbstractTableUI {

    public function __construct($postID) {
        parent::__construct($postID);
    }

    protected function displayTBodyStart() {
        echo '<tbody id="the-list-contest">
            <tr class="no-items" style="display: none" id="comment-contest-not-found-tr"><td class="colspanchange" colspan="' . $this->get_column_count() . '">';
    }
    
    protected function displayTrTable($commentID, $rowClass) {
        echo "<tr id='comment-contest-$commentID'$rowClass>";
    }

    protected function getActions($commentID) {
         $actions = array(
            'delete' => "<a href='javascript:;' onclick='commentContestDelete($commentID)' class='delete'>" . __('Delete', "comment-contest") . '</a>');
         return $actions;
    }
    
    public function get_bulk_actions() {
	$actions = array('delete' => __('Delete', "comment-contest"));
        return $actions;
    }

    protected function getViewFunction($roleID) {
        return "selectRoleInContest(\"$roleID\")";
    }
}
?>
