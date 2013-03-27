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
 * Table result
 */
class OrgZhyweb_WPCommentContest_TableResults extends OrgZhyweb_WPCommentContest_AbstractTableUI {

    public function __construct($postID) {
        parent::__construct($postID);
    }
    
    public function get_columns() {
        // Don't display checkboxes
        return array(
            'author' => __('Author', "comment-contest"),
            'comment' => __('Comment', "comment-contest"));
    }

    protected function displayTBodyStart() {
        echo '<tbody id="the-list-contest">
            <tr class="no-items" style="display: none"><td class="colspanchange" colspan="' . $this->get_column_count() . '">';
    }
    
    protected function displayTrTable($commentID, $rowClass) {
        echo "<tr id='result-comment-contest-$commentID' style='display:none'$rowClass>";
    }

    protected function getActions($commentID) {
        // No action
        return array();
    }
    
    public function get_bulk_actions() {
        // No action
        return array();
    }

    protected function getViewFunction($roleID) {
        // No function
        return "";
    }
}
?>
