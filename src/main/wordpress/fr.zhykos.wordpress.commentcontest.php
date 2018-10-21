<?php
/*
 Plugin Name: Comment Contest
 Plugin URI: https://fr.wordpress.org/plugins/comment-contest/
 Description: Comment Contest allows you to manage contests on your website. This plug-in draws all comments in a specific post and show you the winners.
 Author: Thomas "Zhykos" Cicognani
 Version: 3.0.1
 Author URI: https://github.com/Zhykos/wp-comment-contest
 */

/*
 Copyright (c) 2009, 2018 Comment Contest plug-in for WordPress by Thomas Cicognani

 Contributors:
 Thomas Cicognani - First developments
 Thomas Cicognani - Check compatibility with WordPress 4.3
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

define("ZWPCC_PAGEID", "fr-zhykos-wordpress-commentcontest");
define("ZWPCC_PAGEID_LANG", str_replace("-", ".", ZWPCC_PAGEID));

require_once 'php/WPCommentContest.php';

new WPCommentContestPlugin();

class WPCommentContestPlugin {
    private $pluginDir = '';
    private $pluginSystemPath = '';

    public function __construct() {
        $pluginPath = "/plugins/comment-contest";

        // Initialization
        $this->pluginDir = WP_CONTENT_URL . $pluginPath;
        $this->pluginSystemPath = plugin_dir_path(__FILE__);

        load_plugin_textdomain(ZWPCC_PAGEID_LANG, false, dirname(plugin_basename(__FILE__)).'/lang/');

        add_action('init', array($this, 'init'));

        // Add plug-in system in Wordpress
        // Add a new column in posts admin page
        add_filter('manage_post_posts_columns', array($this, 'zwpcc_column_header'), 10);
        add_action('manage_post_posts_custom_column', array($this, 'zwpcc_column_content'), 10, 2);

        // Add a new column in pages admin page
        add_filter('manage_pages_columns', array($this, 'zwpcc_column_header'), 10);
        add_action('manage_pages_custom_column', array($this, 'zwpcc_column_content'), 10, 2);

        // Add menu
        add_action('admin_menu', array($this, 'zwpcc_addPluginMenu'), 10, 2);
    }

    /**
     * Plug-in initialisation
     */
    public function init() {
        // Load Javascript and CSS
        add_action('admin_enqueue_scripts', array($this, 'zwpcc_loadJsCSS'));
    }

    /**
     * Load Javascript and CSS
     * Wordpress Action : admin_enqueue_scripts.
     */
    public function zwpcc_loadJsCSS() {
        // Comment Contest Javascript file (needs jQuery, jQueryUI, jQueryUI Dialog and jQueryUI DatePicker)
        wp_register_script('fr.zhykos.wordpress.commentcontest.js', plugins_url('/js/fr.zhykos.wordpress.commentcontest.js', __FILE__), array('jquery', 'jquery-ui-core', 'jquery-ui-dialog', 'jquery-ui-datepicker'));
        wp_enqueue_script('fr.zhykos.wordpress.commentcontest.js');

        // Tooltips by TipTip (needs jQuery)
        wp_register_script('TipTip.js', plugins_url('/js/jquery.tipTip.minified.js', __FILE__), array('jquery'));
        wp_enqueue_script('TipTip.js');

        // jQuery UI style
        wp_enqueue_style('jquery-style', 'http://ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/themes/smoothness/jquery-ui.min.css');

        // Plugin CSS
        wp_enqueue_style('fr.zhykos.wordpress.commentcontest.css', plugins_url('/css/fr.zhykos.wordpress.commentcontest.css', __FILE__));
    }

    /**
     * Add plug-in menu.
     * This menu doesn't display anything but it usefull because it creates an admin page
     * Wordpress Action : admin_menu.
     */
    public function zwpcc_addPluginMenu() {
        add_comments_page(__("Comment Contest", ZWPCC_PAGEID_LANG), __("Comment Contest", ZWPCC_PAGEID_LANG), 10, ZWPCC_PAGEID_LANG, array(new WPCommentContest($this->pluginDir, $this->pluginSystemPath), 'display'));
    }

    /**
     * Modify the header of posts table.
     * Add the Comment Contest column.
     * Wordpress Filter : manage_post_posts_columns.
     * @param array $columns [dictionary(string => string)] Existing columns (id / name)
     * @return array [dictionary(string => string)] New columns to display
     */
    public function zwpcc_column_header($columns) {
        $columns[ZWPCC_PAGEID] = __('Contest', ZWPCC_PAGEID_LANG);
        return $columns;
    }

    /**
     * Display the content of the contest column. It's a link to contest page.
     * If no comment posted, cannot launch contest.
     * Wordpress Action : manage_post_posts_custom_column.
     * @param string $columnID Column ID
     * @param int $postID Post ID
     */
    public function zwpcc_column_content($columnID, $postID) {
        if ($columnID == ZWPCC_PAGEID) {
            if (get_comments_number($postID) > 0) {
                echo sprintf('<a href="%s?page=%s&amp;postID=%d">%s</a>',
                    admin_url('edit-comments.php'),
                    ZWPCC_PAGEID_LANG,
                    $postID,
                    __("Launch contest", ZWPCC_PAGEID_LANG));
            } else {
                echo __("No comment", ZWPCC_PAGEID_LANG);
            }
        }
    }

}
