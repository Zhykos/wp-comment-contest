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

require_once(ABSPATH . 'wp-admin/includes/class-wp-list-table.php');

/**
 * Abstract table to display comments
 */
abstract class OrgZhyweb_WPCommentContest_AbstractTableUI extends WP_List_Table {
    /** Post ID which contains comments to display */
    private $postID;
    
    /**
     * Display the beginning of the table (table which contains comments).
     * Used to declare different tables with unique IDs.
     */
    abstract protected function displayTBodyStart();
    
    /**
     * Display a table line start tag (&lt;tr ...&gt;).
     * Used to declare different lines with unique IDs.
     * @param int $commentID Comment ID
     * @param string $rowClass Line class (CSS)
     */
    abstract protected function displayTrTable($commentID, $rowClass);
    
    /**
     * Get specific dynamic actions for each line
     * @param int $commentID Comment ID
     * @return array(string=&gt;string) A dictionary with the action IDs and the HTML code to display for each action
     */
    abstract protected function getActions($commentID);
    
    /**
     * Get the Javascript function call which allows to select comments with the same author's role
     * @param int $roleID Role ID
     * @return string Javascript function call
     */
    abstract protected function getViewFunction($roleID);

    public function __construct($postID) {
        parent::__construct();
        add_filter( 'comment_author', 'floated_admin_avatar' );
        $this->postID = $postID;
    }
    
    public function get_columns() {
        return array(
            'cb' => '<input type="checkbox" />',
            'author' => __('Author', "comment-contest"),
            'comment' => __('Comment', "comment-contest"));
    }
    
    public function prepare_items() {
        $_comments = get_comments(array('post_id' => $this->postID,
                                        'status' => 'approve'));
        
        $columns = $this->get_columns();
        $hidden = array();
        $sortable = array();
        $this->_column_headers = array($columns, $hidden, $sortable);
        
        $this->items = $_comments;
    }
    
    public function single_row($item) {
        global $comment;
        static $row_class = '';
        
        $comment = $item;
        $row_class = ( $row_class == '' ? ' class="alternate"' : '' );

        $this->displayTrTable($item->comment_ID, $row_class);
        echo $this->single_row_columns($item);
        echo '</tr>';
    }
    
    // Display user roles
    public function get_views() {
        global $wp_roles;

        $all_roles = $wp_roles->roles;
        $editable_roles = apply_filters('editable_roles', $all_roles);

        $roles = array();
        foreach ($editable_roles as $roleID => $roleParam) {
            $roles[$roleID] = "<a href='javascript:;' onclick='" . $this->getViewFunction($roleID) . "'>" . sprintf(__("Select: %s", "comment-contest"), translate_user_role($roleParam["name"])) . "</a>";
        }
        return $roles;
    }
   
    public function display() {
        extract($this->_args);
        $this->display_tablenav('top');
        echo "<table class=\"wp-list-table " . implode( ' ', $this->get_table_classes() ) . "\" cellspacing=\"0\">
	<thead>
	<tr>";
        $this->print_column_headers();
        echo "</tr>
	</thead>
	<tfoot>
	<tr>";
        $this->print_column_headers(false);
        echo "</tr>
	</tfoot>";
        $this->displayTBodyStart();
        $this->no_items();
        echo '</td></tr>';
        $this->display_rows();
        echo "</tbody></table>";
        $this->display_tablenav('bottom');
    }
    
    /**
     * Display the column with the checkbox
     * @param Object $comment Current comment
     */
    public function column_cb($comment) {
        ?>
        <label class="screen-reader-text" for="cb-select-<?php echo $comment->comment_ID; ?>"><?php _e('Select comment', 'comment-contest'); ?></label>
        <input id="cb-select-<?php echo $comment->comment_ID; ?>" type="checkbox" name="delete_comments[]" value="<?php echo $comment->comment_ID; ?>" />
        <?php
    }
    
    /**
     * Get user roles by user ID.
     * @param  int $id User ID
     * @return array User roles
     * @see http://wordpress.stackexchange.com/a/58921
     */
    private function wpse_58916_user_roles_by_id($id) {
        $user = new WP_User($id);
        if (!is_array($user->roles)) {
            return $user;
        }

        $wp_roles = new WP_Roles;
        $names = $wp_roles->get_names();
        $out = array();

        foreach ($user->roles as $role) {
            if (isset($names[$role])) {
                $out[$role] = $names[$role];
            }
        }
        return $out;
    }
    
    /**
     * Display the column with the author information
     * @param Object $comment Current comment
     */
    public function column_author($comment) {
        global $comment_status;

        $author_url = get_comment_author_url($comment->comment_ID);
        if ('http://' == $author_url) {
            $author_url = '';
        }
        
        $author_url_display = preg_replace('|http://(www\.)?|i', '', $author_url);
        if (strlen($author_url_display) > 50) {
            $author_url_display = substr($author_url_display, 0, 49) . '...';
        }

        echo "<strong>";
        comment_author($comment->comment_ID);
        echo '</strong><br />';
        if (!empty($author_url)) {
            echo "<a title='$author_url' href='$author_url'>$author_url_display</a><br />";
        }

        if (!empty($comment->comment_author_email)) {
            echo "<a href='mailto:$comment->comment_author_email'>$comment->comment_author_email</a><br />";
        }
        
        echo '<a href="edit-comments.php?s=';
        comment_author_IP($comment->comment_ID);
        echo '&amp;mode=detail';
        if ('spam' == $comment_status) {
            echo '&amp;comment_status=spam';
        }
        echo '">';
        comment_author_IP($comment->comment_ID);
        echo '</a><br />';
        
        // Add role in UI : used to select all users with the same role
        echo '<span style="display:none" class="zhyweb_comment_contest_role">';
        $roles = $this->wpse_58916_user_roles_by_id(get_user_by('login', get_comment_author($comment->comment_ID)));
        echo implode('|', array_keys($roles));
        echo '</span>';
        
        // Add comment ID (used to get winners)
        echo '<span style="display:none" class="zhyweb_comment_contest_id">' . $comment->comment_ID . '</span>';
        
        // Add comment timestamp post
        echo '<span style="display:none" class="zhyweb_comment_contest_timestamp">' . get_comment_date('YmdHi', $comment->comment_ID) . '</span>';
        
        // Add comment IP address
        echo '<span style="display:none" class="zhyweb_comment_contest_ip">' . get_comment_author_IP($comment->comment_ID) . '</span>';
        
        // Add comment email
        echo '<span style="display:none" class="zhyweb_comment_contest_email">' . $comment->comment_author_email . '</span>';
    }
    
    /**
     * Display the column with the user's comment
     * @param Object $comment Current comment
     */
    public function column_comment($comment) {
        $comment_url = esc_url(get_comment_link($comment->comment_ID));

        echo '<div class="submitted-on">';
        /* translators: 2: comment date, 3: comment time */
        printf(__('Submitted on <a href="%1$s">%2$s at %3$s</a>', "comment-contest"), $comment_url,
                /* translators: comment date format. See http://php.net/date */ get_comment_date(__('Y/m/d', "comment-contest"), $comment->comment_ID),
                /* translators: comment time format. See http://php.net/date */ get_comment_date(get_option('time_format'), $comment->comment_ID));

        if ($comment->comment_parent) {
            $parent = get_comment($comment->comment_parent);
            $parent_link = esc_url(get_comment_link($comment->comment_parent));
            $name = get_comment_author($parent->comment_ID);
            printf(' | ' . __('In reply to <a href="%1$s">%2$s</a>.', "comment-contest"), $parent_link, $name);
        }

        echo '</div>';
        comment_text($comment->comment_ID);
        ?>
            <div id="inline-<?php echo $comment->comment_ID; ?>" class="hidden">
            <textarea class="comment" rows="1" cols="1"><?php echo esc_textarea(apply_filters('comment_edit_pre', $comment->comment_content)); ?></textarea>
            <div class="author-email"><?php echo esc_attr($comment->comment_author_email); ?></div>
            <div class="author"><?php echo esc_attr($comment->comment_author); ?></div>
            <div class="author-url"><?php echo esc_attr($comment->comment_author_url); ?></div>
            <div class="comment_status"><?php echo $comment->comment_approved; ?></div>
            </div>
        <?php
        
        $actions = $this->getActions($comment->comment_ID);
        
        $i = 0;
        echo '<div class="row-actions">';
        foreach ($actions as $action => $link) {
            ++$i;
            $sep = '';
            if ($i == 3) {
                $sep = ' | ';
            }
            echo "<span class='$action'>$sep$link</span>";
        }
        echo '</div>';
    }    
}
?>
