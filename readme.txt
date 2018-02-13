=== Plugin Name ===
Contributors: zhykos
Tags: comments, comment, contest, draw, concours, commentaire, zhykos
Requires at least: 3.3
Tested up to: 4.9.4
Stable tag: trunk
License: GPLv2 or later
License URI: http://www.gnu.org/licenses/gpl-2.0.html

Comment Contest allows you to manage contests on your website. This plug-in draws all comments in a specific post and show you the winners.

== Description ==

If you want to organize a contest on your blog (some goodies, games... to win), use this plugin. Comment Contest works only with comments.
You choose some comments, set some features (winners' number ...) and the system chooses the winners for you.

== Installation ==

This section describes how to install the plugin and get it working.

1. Upload directory `comment-contest` to the `/wp-content/plugins/` directory
2. Activate the plugin through the 'Plugins' menu in WordPress
3. Go to 'Posts' and select 'Launch contest' on a post

== Frequently Asked Questions ==

= Which PHP version I need? =

You need PHP version 5 like WorPress.

= What are the available languages? =

* English by Thomas "Zhykos" Cicognani (since 1.1) ;
* French by Thomas "Zhykos" Cicognani (since 1.0) ;
* Spanish by Andrew Kurtis (since 2.1.3 / partial since 2.2) ;
* Belorussian by P.C. (since 1.40.1 / partial since 2.0) ;
* Dutch by Rene (since 1.41.1 / partial since 2.0)

== Screenshots ==

1. Plug-in main page. Used to show some information.
2. Choose the article for the contest
3. Manage the comments
4. Choose to delete a comment from the contest
5. You can restore deleted comments (deleted comments are red)
6. Cheat: this comment will win! (cheating comments are green)
7. Filters
8. Help
9. Result table and notification after saving winners

== Changelog ==

= 2.4.1 =
* Misc: Check compatibility with WordPress 4.3

= 2.4.0 =
* New: Winners can be saved in post custom fields
* Misc: Check compatibility with WordPress 4.2
* Misc: You can now find this plug-in on Git! Go check out (https://github.com/Zhykos/wp-comment-contest) and be free to contribute!

= 2.3.0 =
* New: Contest can be launch from pages (before only posts could)
* New: Add new filters
* Update: Change the main plug-in page with my contact info and a system to send me info when a bug occurs
* Misc: Check compatibility with WordPress 4.1
* Update: A lot of internal improvements

= 2.2.3 =
* Misc: Check compatibility with WordPress 4.0
* Misc: Add plugin icon for WordPress 4.0

= 2.2.2 =
* Misc: Check compatibility with WordPress 3.9

= 2.2.1 =
* Misc: Check compatibility with WordPress 3.8.1

= 2.2 =
* New: Add filters to select comments posted after a date, to select comments with same IP address or email
* Misc: New screenshots

= 2.1.3 =
* Misc: Add Spanish language. Thanks to Andrew from http://www.webhostinghub.com/ for this contribution!

= 2.1.2 =
* Misc: Remove plugin image to be 3.8 style compliant
* Misc: Check compatibility with WordPress 3.8

= 2.1.1 =
* Fix: Conflict with the plugin "WP RSS Aggregator" because I used a reserved URL parameter (thank you Juergen)
* Update: Add the URL of WordPress page in the plugin information page
* Misc: Check compatibility with WordPress 3.7.1

= 2.1 =
* Fix: Result array wasn't sorted (most recent comment was always on the top)
* New: Add cheat. Selected comments always win
* New: Add help
* Update: Only use one table to choose comments
* Misc: Check compatibility with WordPress 3.6
* Misc: Minimize Javascript and CSS

= 2.0 =
* All new architecture : more "WordPress" compliant
* Contest are now launched from posts page
* Some features have disappeared since this version and will be added later.

= 1.41.1 =
* Add Dutch language. Thanks to Rene from http://wpwebshop.com/premium-wordpress-themes/ for this contribution!
* Compatibility tests with WordPress 3.0.2

= 1.41 =
* Change display of the winners and losers list at the end
* Compatibility tests with WordPress 3.0

= 1.40.1 =
* Add Belorussian language. Thanks to P.C from http://pc.de for this contribution!

= 1.4 =
* Tests with WordPress 2.9.2
* Add an editor to write the email
* Add a message to send to losers
* Email subject can be changed
* Display winners and losers emails at the end (in case automatic email fails for example)

= 1.37 =
* Some little improvements

= 1.36 =
* If you use MySQL version 4.0, the plugin doesn't work because sub-querys are not compatible with MySQL 4.0. You have to use MySQL 4.1.xxx at least but some website provider don't allow us to choose our version. Sub-queries are now removed.

= 1.35 =
* Only display posts with comments
* BUG FIX : Simple and double quotes protected because if a pseudo contains quotes, some query bug (thanks to Kamel from www.yoocom.fr)
* BUG FIX : Change the place of a check value (bug if the value was null)
* Different names check for prizes and code optimization
* Remove case sensitive sort for displaying all the participants
* Change error message format in certain cases
* Change localization message in French

= 1.3 =
* Change error message display (now it's the same message as WordPress). Old values are put in the fields so the user don't have to type again the values
* New winners display

= 1.2 =
* Set the prices to win
* Change PHP version for the main class. I migrate from PHP5 to version 4 because some servers still use PHP4 and PHP4 is the WordPress recommendation

= 1.1.2 =
* Add the possibility to choose between Normal Contest or Speed Contest. Speed Contest choose winners by sorting them by chronological order

= 1.1.1 =
* Bug fix : send a mail to winners (tested online)
* Bug fix : can display other posts

= 1.1.1b =
* Send a mail to the winners at the end. You can choose to do so or not (not tested yet)

= 1.1.0.1 =
* Remove "plugin only in french" in a comment

= 1.1 =
* Translation in English and French
* Comments are included in the source code
* Screenshots are available in the install

= 1.0 =
* Release version
* Only French language is available

== Upgrade Notice ==

= 2.0 =
* All new architecture.
* Compatibility with WordPress 3.5.1

= 2.1 =
* New table
* Cheat and help added
* Compatibility with WordPress 3.6

= 2.2 =
* New features to filter comments

== Credits ==

= Images =
* Help icon by http://www.visualpharm.com/
* Plus and Minus icons by http://www.yanlu.de 