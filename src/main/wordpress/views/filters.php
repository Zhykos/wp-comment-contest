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

?>

<div style="clear:both;">
    <span>
        <a href="javascript:;" onclick="toggleFilters('<?php echo $this->pluginDir; ?>')"><img src="<?php echo $this->pluginDir; ?>/img/plus.png" alt="expand/collapse" id="filtersImg" style="vertical-align: middle;" /></a>
        <a href="javascript:;" onclick="toggleFilters('<?php echo $this->pluginDir; ?>')"><?php _e('Filters', "fr.zhykos.wordpress.commentcontest"); ?></a>
    </span>
    <div id="filters" style="display: none;">
        <h4><?php _e('Contest deadline', "fr.zhykos.wordpress.commentcontest"); ?></h4>
        <div id="zwpcc_dateFilter_error_message" style="color: red; display: none;"><?php _e('Wrong date format!', "fr.zhykos.wordpress.commentcontest"); ?></div>
        <?php _e('Date:', "fr.zhykos.wordpress.commentcontest"); ?> <input type="text" id="datepicker" />
        <br /><?php _e('Hour (24h format):', "fr.zhykos.wordpress.commentcontest"); ?> <input type="text" id="dateHours" maxlength="2" size="3" /><?php _e('h', "fr.zhykos.wordpress.commentcontest"); ?> <input type="text" id="dateMinutes" maxlength="2" size="3" /><?php _e('min', "fr.zhykos.wordpress.commentcontest"); ?><br />
        <br /><input type="button" class="button action" id="dateSubmit" value="<?php _e('Select comments after this deadline', "fr.zhykos.wordpress.commentcontest"); ?>" />

        <br /><br />
        <h4><?php _e('Name', "fr.zhykos.wordpress.commentcontest"); echo "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("You can allow people with the same alias to post several comments for the contest. This filter limits the maximum number of comments for the same person.<br />(ex. 0 means a person is not allowed to post more than one time; 2 means only two comments from the same person will be kept for the contest)", "fr.zhykos.wordpress.commentcontest") . "\" />"; ?></h4>
        <div id="zwpcc_aliasFilter_error_message" style="color: red; display: none;"><?php _e('Wrong configuration! This number must be equals or greater than zero', "fr.zhykos.wordpress.commentcontest"); ?></div>
        <?php _e('Same name maximum use:', "fr.zhykos.wordpress.commentcontest"); ?> <input type="text" id="aliasConfig" maxlength="2" size="3" value="1" />
        <br /><input type="button" class="button action" id="aliasAddressFilter" value="<?php _e('Select comments with the same name', "fr.zhykos.wordpress.commentcontest"); ?>" />

        <br /><br />
        <h4><?php _e('Email address', "fr.zhykos.wordpress.commentcontest"); echo "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("You can allow people with the same email to post several comments for the contest. This filter limits the maximum number of comments for the same email.<br />(ex. 0 means an email is not allowed to post more than one time; 2 means only two comments from the same email will be kept for the contest)", "fr.zhykos.wordpress.commentcontest") . "\" />"; ?></h4>
        <div id="zwpcc_emailFilter_error_message" style="color: red; display: none;"><?php _e('Wrong configuration! This number must be equals or greater than zero', "fr.zhykos.wordpress.commentcontest"); ?></div>
        <?php _e('Same email maximum use:', "fr.zhykos.wordpress.commentcontest"); ?> <input type="text" id="emailConfig" maxlength="2" size="3" value="1" />
        <br /><input type="button" class="button action" id="emailAddressFilter" value="<?php _e('Select comments with the same email', "fr.zhykos.wordpress.commentcontest"); ?>" />


        <br /><br />
        <h4><?php _e('IP address', "fr.zhykos.wordpress.commentcontest"); echo "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("You can allow people with the same IP address to post several comments for the contest. This filter limits the maximum number of comments for the same IP address.<br />(ex. 0 means a IP address is not allowed to post more than one time; 2 means only two comments from the same IP address will be kept for the contest)", "fr.zhykos.wordpress.commentcontest") . "\" />"; ?></h4>
        <div id="zwpcc_ipFilter_error_message" style="color: red; display: none;"><?php _e('Wrong configuration! This number must be equals or greater than zero', "fr.zhykos.wordpress.commentcontest"); ?></div>
        <?php _e('Same IP address maximum use:', "fr.zhykos.wordpress.commentcontest"); ?> <input type="text" id="ipConfig" maxlength="2" size="3" value="1" />
        <br /><input type="button" class="button action" id="ipAddressFilter" value="<?php _e('Select comments with the same IP address', "fr.zhykos.wordpress.commentcontest"); ?>" />

        <br /><br />
        <h4><?php _e('Comment', "fr.zhykos.wordpress.commentcontest"); ?></h4>
        <?php _e('Words (comma separated):', "fr.zhykos.wordpress.commentcontest"); ?> <input type="text" id="words" size="30" />
        <br /><input type="button" class="button action" id="wordsFilter" value="<?php _e('Select comments which don\'t contain one of these words', "fr.zhykos.wordpress.commentcontest"); ?>" />
        <br /><input type="button" class="button action" id="allWordsFilter" value="<?php _e('Select comments which don\'t contain all these words', "fr.zhykos.wordpress.commentcontest"); ?>" />

        <br /><br />
        <h4><?php _e('Time between two comments', "fr.zhykos.wordpress.commentcontest"); echo "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("You can allow people to post several comments in your contest. Between two comments the person must wait some times. This filter allow you to get all comments (from a person who has the same name/email/IP address) which don't respect your configuration", "fr.zhykos.wordpress.commentcontest") . "\" />"; ?></h4>
        <div id="zwpcc_timeBetweenFilter_error_message" style="color: red; display: none;"><?php _e('Wrong configuration! This number must be greater than zero and at least one criterion must be checked!', "fr.zhykos.wordpress.commentcontest"); ?></div>
        <?php _e('Time (in minutes [1 day = 1440 min]):', "fr.zhykos.wordpress.commentcontest"); ?> <input type="text" id="timeBetween" size="10" maxlength="6" />
        <br /><?php _e('Criteria:', "fr.zhykos.wordpress.commentcontest"); ?>
        <input type="checkbox" id="timeBetweenFilterName" checked="checked" /><label for="timeBetweenFilterName"><?php _e('Name', "fr.zhykos.wordpress.commentcontest"); ?></label>
         / <input type="checkbox" id="timeBetweenFilterEmail" /><label for="timeBetweenFilterEmail"><?php _e('Email', "fr.zhykos.wordpress.commentcontest"); ?></label>
         / <input type="checkbox" id="timeBetweenFilterIP" /><label for="timeBetweenFilterIP"><?php _e('IP address', "fr.zhykos.wordpress.commentcontest"); ?></label>
        <br /><input type="button" class="button action" id="timeBetweenFilter" value="<?php _e('Select comments posted too soon', "fr.zhykos.wordpress.commentcontest"); ?>" />
    </div>
</div>