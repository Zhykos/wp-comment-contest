<?php
/*
  Plugin Name: Comment Contest
  Plugin URI: http://wp-comment-contest.zhyweb.org/
  Description: If you create a contest on your website, you can draw all comments in a specific post
  Author: Thomas "Zhykos" Cicognani
  Version: 2.1.2
  Author URI: http://www.zhyweb.org/
 */

/*
  Copyright 2009 - 2013 Comment Contest plug-in for Wordpress by Thomas "Zhykos" Cicognani  (email : tcicognani@zhyweb.org)

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

define(ORG_ZHYWEB_WP_COMMENT_CONTEST_PAGE_ID, "orgZhyweb-wpCommentContest");

require_once 'php/OrgZhyweb_WPCommentContest_MainUI.php';

new CommentContest();

class CommentContest {
    private $localizationName = "comment-contest";
    
    private $pluginDir = '';
    
    public function __construct() {
        $pluginPath = "/plugins/$this->localizationName";
        
         // Initialization
        $this->pluginDir = WP_CONTENT_URL . $pluginPath;
        
        load_plugin_textdomain($this->localizationName, false, dirname(plugin_basename(__FILE__)).'/lang/');
        
        add_action('init', array($this, 'init'));
        
        // Add plug-in system in Wordpress
        add_filter('manage_post_posts_columns', array($this, 'orgZhyweb_wpCommentContest_column_header'), 10);
        add_action('manage_post_posts_custom_column', array($this, 'orgZhyweb_wpCommentContest_column_content'), 10, 2);
        
        // Add menu
        add_action('admin_menu', array($this, 'orgZhyweb_wpCommentContest_addPluginMenu'), 10, 2);
    }
    
    /**
     * Plug-in initialisation
     */
    public function init() {
        // Load Javascript and CSS
        add_action('admin_enqueue_scripts', array($this, 'orgZhyweb_wpCommentContest_loadJsCSS'));
    }

    /**
     * Load Javascript and CSS
     * Wordpress Action : admin_enqueue_scripts.
     */
    public function orgZhyweb_wpCommentContest_loadJsCSS() {
        // Comment Contest Javascript file (needs jQuery, jQueryUI and jQueryUI Dialog)
        wp_register_script('OrgZhywebWPCommentContest.js', plugins_url('/js/OrgZhyweb_WPCommentContest_jQuery.min.js', __FILE__), array('jquery', 'jquery-ui-core', 'jquery-ui-dialog'));
        wp_enqueue_script('OrgZhywebWPCommentContest.js');
                
        // Tooltips by TipTip (needs jQuery)
        wp_register_script('TipTip.js', plugins_url('/js/jquery.tipTip.minified.js', __FILE__), array('jquery'));
        wp_enqueue_script('TipTip.js');
        
        // jQueryUI Dialog style
        wp_enqueue_style('wp-jquery-ui-dialog');
        
        // Plugin CSS
        wp_enqueue_style('comment-contest.css', plugins_url('/css/comment-contest.min.css', __FILE__));
    }
    
    /**
     * Add plug-in menu.
     * This menu doesn't display anything but it usefull because it creates an admin page
     * Wordpress Action : admin_menu.
     */
    public function orgZhyweb_wpCommentContest_addPluginMenu() {
        add_comments_page(__("Comment Contest", "comment-contest"), __("Comment Contest", "comment-contest"), 10, ORG_ZHYWEB_WP_COMMENT_CONTEST_PAGE_ID, array(new OrgZhyweb_WPCommentContest_MainUI($this->pluginDir), 'display'));
    }

    /**
     * Modify the header of posts table.
     * Add the Comment Contest column.
     * Wordpress Filter : manage_post_posts_columns.
     * @param array $columns [dictionary(string => string)] Existing columns (id / name)
     * @return array [dictionary(string => string)] New columns to display
     */
    public function orgZhyweb_wpCommentContest_column_header($columns) {
        $columns[ORG_ZHYWEB_WP_COMMENT_CONTEST_PAGE_ID] = __('Contest', "comment-contest");
        return $columns;
    }
    
    /**
     * Display the content of the contest column. It's a link to contest page.
     * If no comment posted, cannot launch contest.
     * Wordpress Action : manage_post_posts_custom_column.
     * @param string $columnID Column ID
     * @param int $postID Post ID
     */
    public function orgZhyweb_wpCommentContest_column_content($columnID, $postID) {
        if ($columnID == ORG_ZHYWEB_WP_COMMENT_CONTEST_PAGE_ID) {
            if (get_comments_number($postID) > 0) {
                echo sprintf('<a href="%s?page=%s&amp;postID=%d">%s</a>',
                        admin_url('edit-comments.php'),
                        ORG_ZHYWEB_WP_COMMENT_CONTEST_PAGE_ID,
                        $postID,
                        __("Launch contest", "comment-contest"));
            } else {
                echo __("No comment", "comment-contest");
            }
        }
    }
}
?>