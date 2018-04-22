#include <android/log.h>
#include "track.h"

namespace reloc {
    Track::Track() {};
    
    Track::Track(cv::Mat &img, int isJ, int method) : isJ_(isJ), method_(method){
        cur_id_ = 0;
        down_num_ = 0;
        up_num_ = 0;
        now_model_ = 0;
        lk_model_ = 0;
        lk_num_ = 0;
        score_ = -1;
        lk_init_num_ = -1;

        top = 0;    //Frame stack
        
        orb_ori_ = cv::ORB::create(1000, 1.2, 8, 31, 0, 2, cv::ORB::HARRIS_SCORE, 10, 20);
        orb_com_ = cv::ORB::create(1000, 1.2, 8, 31, 0, 2, cv::ORB::HARRIS_SCORE, 10, 20);
        orb_ori_t_ = new ORBextractor(1000, 1.2, 8, 20, 7);
        orb_com_t_ = new ORBextractor(1000, 1.2, 8, 20, 7);
        orb_com_old_t_ = new ORBextractor(1000, 1.2, 8, 20, 7);
        rect_scale_ = 1.0 / 4;
        img_scale_ = 0;
        
        //cv::resize(img, img, cv::Size(img.cols * img_scale_, img.rows * img_scale_));
        img.copyTo(ori_img_);
        cv::cvtColor(ori_img_,ori_img_,CV_RGB2GRAY);

        center_ = cv::Point2f(img.cols / 2, img.rows / 2);
        ori_frame_old_ = Frame(cur_id_, orb_ori_);
        ori_frame_ = Frame(cur_id_++, orb_ori_);
        ori_frame_.rect[0] = cv::Point2f(center_.x - img.cols * rect_scale_, center_.y - img.rows * rect_scale_);
        ori_frame_.rect[1] = cv::Point2f(center_.x + img.cols * rect_scale_, center_.y - img.rows * rect_scale_);
        ori_frame_.rect[2] = cv::Point2f(center_.x + img.cols * rect_scale_, center_.y + img.rows * rect_scale_);
        ori_frame_.rect[3] = cv::Point2f(center_.x - img.cols * rect_scale_, center_.y + img.rows * rect_scale_);

        ori_frame_old_.rect[0] = cv::Point2f(center_.x - img.cols * rect_scale_, center_.y - img.rows * rect_scale_);
        ori_frame_old_.rect[1] = cv::Point2f(center_.x + img.cols * rect_scale_, center_.y - img.rows * rect_scale_);
        ori_frame_old_.rect[2] = cv::Point2f(center_.x + img.cols * rect_scale_, center_.y + img.rows * rect_scale_);
        ori_frame_old_.rect[3] = cv::Point2f(center_.x - img.cols * rect_scale_, center_.y + img.rows * rect_scale_);


        ori_frame_.setORBT(orb_ori_t_);
        ori_frame_.extractORB(ori_img_);
        //ori_frame_old_.setORBT(orb_ori_t_);
        //ori_frame_old_.extractORB(ori_img_);
        num_p_.resize(ori_frame_.kpts_kp_.size());
        ori_frame_.inlier_num_ = 1000;
        ori_frame_old_.inlier_num_ = 1000;
        ref_frame_ = ori_frame_;
    }
    
    cv::Mat Track::tracking(cv::Mat &img) {
        if (method_ == 2) {
            return 0.3 * img + 0.7 * ori_img_;
        }
        std::cout << "img id : " << cur_id_ << " " << now_model_ << " " << top << std::endl;
        uchar *data = img.ptr<uchar>(0);
        //cv::resize(img, img, cv::Size(img.cols * img_scale_, img.rows * img_scale_));
        img.copyTo(cur_img_);
        cv::cvtColor(cur_img_,cur_img_,CV_RGB2GRAY);

        cur_frame_ = Frame(cur_id_++, orb_ori_);
        cur_frame_old_ = Frame(cur_id_ - 1, orb_com_);
        if (last_frame_.inlier_ref_num_ < 50) lk_model_ = false;

        if (lk_model_ == false) lk_init_num_ = -1;
        __android_log_print(ANDROID_LOG_INFO, "info", "cur_id: %d with Ref: %d ref num : %d use lk : %d lk:num : %d", cur_id_, now_model_, top, lk_model_, lk_num_);
        if (method_ == 0) {
            cur_frame_.setORBT(orb_com_t_);
            if (lk_model_) {
                trackUseLK(cur_img_);
            } else {
                trackUseKeyPoint(cur_img_);
            }
        }

        if (method_ != 0) {
            //cur_frame_old_.setORBT(orb_com_old_t_);
            trackUseKeyPointToOld(cur_img_);
        }

        //printf("Error %lf:\n", cur_frame_.getError(ori_frame_));
        //cv::Mat output;
        //output = drawInfo(img, cur_frame_, cv::Scalar(255, 0, 0));
        //cv::imshow("asd", output);

        //cv::Mat match_img = drawMatch(img, cur_frame_);
        //cv::imshow("jjj", match_img);

        //cv::waitKey(isJ_);

        if (method_ == 0) {
            getDisToOriUseRect(img, cur_frame_);

            if (cur_frame_.inlier_ref_num_ < (inlier_num_[0] + inlier_num_[1]) / 2 ||
                cur_frame_.getDis(ref_frame_) > 80) {
                down_num_++;
                if (cur_frame_.getDis(ref_frame_) > 160) down_num_ ++;
            } else down_num_ = 0;

            inlier_num_[1] = inlier_num_[0];
            inlier_num_[2] = inlier_num_[1];
            inlier_num_[0] = cur_frame_.inlier_ref_num_;


            changeModel();
            if (lk_model_ && lk_init_num_ == -1) lk_init_num_ = in_num_;
        }

        if (method_ == 0)
            return cur_frame_.h_;
        if (method_ == 1)
            return cur_frame_old_.h_;

        
    }
    
    void Track::changeModel() {
        std::cout << cur_frame_.dis2ori_ << " " << ref_frame_.dis2ori_ << " " << cur_frame_.getDis(ref_frame_) << std::endl;
        //__android_log_print(ANDROID_LOG_INFO, "info", "%f %f %f", cur_frame_.dis2ori_, ref_frame_.dis2ori_, cur_frame_.getDis(ref_frame_));
        // track ref
        if (now_model_ == 1) {
            //model change
            int best_id = -1;
            double best_dis = cur_frame_.dis2ori_;
            for (int i = 0; i < top; i++) {
                if (best_dis > cur_frame_.getDis(stack[i])) {
                    best_dis = cur_frame_.getDis(stack[i]);
                    best_id = i;
                }
            }
            if (best_id != top - 1) {
                //__android_log_print(ANDROID_LOG_INFO, "info", "best_id : %d", best_id);
                up_num_++;
                if (up_num_ >= 3) {
                    if (best_id == -1) {
                        top = 0;
                        now_model_ = 0;
                        ref_frame_ = ori_frame_;
                    } else {
                        top = best_id + 1;
                        ref_frame_ = stack[best_id];
                    }
                    down_num_ = 0;
                    lk_model_ = false;
                    last_frame_ = cur_frame_;
                    cur_img_.copyTo(last_img_);
                    return ;
                } 
            }
        }
        //update ref
        std::cout << "Down" << down_num_ << std::endl;
        if (down_num_ >= 5) {
            std::cout << "REFNUM: " << cur_frame_.inlier_ref_num_ << std::endl;
            //__android_log_print(ANDROID_LOG_INFO, "info", "REFNUM: %d" , cur_frame_.inlier_ref_num_);
            std::cout << cur_frame_.getError(ori_frame_) << std::endl;
            std::cout << top << std::endl;
            //need
            if (!lk_model_ && cur_frame_.inlier_ref_num_ > 6000 / cur_frame_.getDis(ref_frame_) && cur_frame_.inlier_ref_num_ > std::max(30, last_frame_.inlier_ref_num_ / 5 * 4) && top < 5 && cur_frame_.getDis(ref_frame_) > 30) {
                ref_frame_ = cur_frame_;
                stack[top++] = cur_frame_;
                down_num_ = 0;
                up_num_ = 0;
                now_model_ = 1;
                lk_model_ = false;
            } else {
                if (lk_model_) {
                    lk_model_ = false;
                    lk_num_ = 0;
                } else {
                    if (lk_model_) {
                        lk_num_++;
                        //need
                        if (lk_num_ >= 5 && cur_frame_.inlier_ref_num_ < lk_init_num_ - lk_init_num_ / 10) lk_model_ = false, lk_num_ = 0;
                        if (lk_num_ >= 10) lk_model_ = false, lk_num_ = 0;
                    } else {
                        lk_num_ = 0;
                        lk_model_ = true;
                    }
                    down_num_ = 0;
                }
            }
        } else {
            if (lk_model_) {
                lk_num_++;
                if (lk_num_ >= 5 && cur_frame_.inlier_ref_num_ < lk_init_num_ - lk_init_num_ / 10) lk_model_ = false, lk_num_ = 0;
                if (lk_num_ >= 10) lk_model_ = false, lk_num_ = 0;
            } else {
                lk_num_ = 0;
                lk_model_ = true;
            }
        }
        
        last_frame_ = cur_frame_;
        cur_img_.copyTo(last_img_);

    }
    
    void Track::trackUseKeyPoint(cv::Mat &img) {
        cur_frame_.extractORB(img);
        //__android_log_print(ANDROID_LOG_INFO, "info", "point num : %d", cur_frame_.kpts_kp_.size());
        std::vector<cv::DMatch> matches;
        
        getMatchUseKnn(cur_frame_, ref_frame_, matches);
        updateMatch(cur_frame_, ref_frame_, matches);
        cur_frame_.updateCorUseKpt(matches, ref_frame_);
        
        cv::Mat h;
        computeHomo(cur_frame_, ref_frame_, h);
        
        cur_frame_.h_ = h;
    }
    
    void Track::trackUseKeyPointToOld(cv::Mat &img) {
        cur_frame_old_.extractORB(img);
        std::vector<cv::DMatch> matches;
        
        getMatchUseKnn(cur_frame_old_, ori_frame_old_, matches);
        cur_frame_old_.updateCorUseKpt(matches, ori_frame_old_);
        
        cv::Mat h;
        computeHomo(cur_frame_old_, ori_frame_old_, h);
        
        cur_frame_old_.h_ = h;
    }
    
    void Track::trackUseLK(cv::Mat &img) {
        std::vector<unsigned char> status;
        std::vector<float> error;
        std::vector<cv::Point2f> kpts_tmp;
        uchar* data = img.ptr<uchar>(0);
        data = last_img_.ptr<uchar>(0);
        cv::calcOpticalFlowPyrLK(last_img_, img, last_frame_.kpts_p2f_, kpts_tmp, status, error, cv::Size(10, 10), 3);
        
        int num = 0;
        for (size_t i = 0; i < kpts_tmp.size(); i++) {
            if (status[i] && last_frame_.cor_id_ref_[i] != -1) {
                cur_frame_.kpts_p2f_.push_back(kpts_tmp[i]);
                cur_frame_.cor_id_ori_.push_back(last_frame_.cor_id_ori_[i]);
                cur_frame_.cor_id_ref_.push_back(last_frame_.cor_id_ref_[i]);
                num++;
            }
        }
        cv::Mat h;
        computeHomo(cur_frame_, ref_frame_, h);
        cur_frame_.h_ = h;
    }
    
    void Track::getMatchUseKnn(Frame &f1, Frame &f2, std::vector<cv::DMatch> &matches) {
        double start = now_ms();
        cv::BFMatcher matcher(cv::NORM_HAMMING);
        std::vector<std::vector<cv::DMatch> > knnMatches;
        matcher.knnMatch(f1.desc_, f2.desc_, knnMatches, 2);
        double stop = now_ms();
        __android_log_print(ANDROID_LOG_INFO, "info", "KNN time:%lf", (stop - start));
        for (size_t i = 0; i < knnMatches.size(); i++) {
            const cv::DMatch &bestMatch = knnMatches[i][0];
            const cv::DMatch &betterMatch = knnMatches[i][1];
            
            float distanceRadio = bestMatch.distance / betterMatch.distance;
            
            if (distanceRadio < 0.8) {
                matches.push_back(bestMatch);
            }
        }
        stop = now_ms();
        __android_log_print(ANDROID_LOG_INFO, "info", "KNN time:%lf", (stop - start));
    }
    void Track::updateMatch(Frame &f1, Frame &f2, std::vector<cv::DMatch> &matches) {
        std::vector<int> rotate[30];
        for (size_t i = 0; i < matches.size(); i++) {
            float rot = f1.kpts_kp_[matches[i].queryIdx].angle - f2.kpts_kp_[matches[i].trainIdx].angle;
            if (rot < 0) rot += 360;
            int now = std::floor(rot / 30);
            rotate[now].push_back(i);
        }

        std::vector<cv::DMatch> new_matches;
        std::pair<int, int> tt[30];
        for (int i = 0; i < 30; i++) {
            tt[i] = std::make_pair(rotate[i].size(), i);
        }
        std::sort(tt, tt + 30);
        for (int i = 29; i >= 27; i--) {
            for (auto id : rotate[tt[i].second]) {
                new_matches.push_back(matches[id]);
            }
        }
        matches = new_matches;
    }

    void Track::computeHomo(Frame &f1, Frame &f2, cv::Mat &h) {
        std::chrono::steady_clock::time_point t1 = std::chrono::steady_clock::now();
        
        std::vector<cv::Point2f> kpts1, kpts2;
        std::vector<int> ids;
        std::vector<uchar> inliers;
        for (size_t i = 0; i < f1.kpts_p2f_.size(); i++) {
            int nid = f1.cor_id_ref_[i];
            int oid = f1.cor_id_ori_[i];
            if (nid != -1) {
                ids.push_back(i);
                kpts1.push_back(f1.kpts_p2f_[i]);
                if (!f2.is_ori_) {
                    if (oid == -1)
                        kpts2.push_back(f2.getHPoint(f2.kpts_p2f_[nid]));
                    else 
                        kpts2.push_back(ori_frame_.kpts_p2f_[oid]);
                } else 
                    kpts2.push_back(f2.kpts_p2f_[nid]);
            }
        }
        inliers.resize(kpts1.size());
        h = cv::findHomography(kpts1, kpts2, CV_RANSAC, 5, inliers, 200, 0.999);
        
        out_num_ = 0, in_num_ = 0;
        match_ = f1;
        double all = 0;
        int num = 0;
        for (size_t i = 0; i < inliers.size(); i++) {
            if (!inliers[i]) {
                out_num_++;
                int id = ids[i];
                //match_.cor_id_ref_[id] = -1;
                //match_.cor_id_ori_[id] = -1;
            } else {
                if (method_ == 0) {
                    updateD(f1.cor_id_ori_[ids[i]], f1.desc_.ptr(ids[i]));
                }
                in_num_++;
                int id = f1.cor_id_ori_[ids[i]];
                if (id != -1) {
                    //__android_log_print(ANDROID_LOG_INFO, "info", "id:%d", id);
                    if (method_ == 0) {
                        all += std::sqrt(
                                pow(floor(
                                        f1.kpts_p2f_[ids[i]].x - ori_frame_.kpts_p2f_[id].x + 0.5),
                                    2) +
                                pow(floor(
                                        f1.kpts_p2f_[ids[i]].y - ori_frame_.kpts_p2f_[id].y + 0.5),
                                    2));
                        num++;
                    } else {

                        all += std::sqrt(
                                pow(floor(
                                        f1.kpts_p2f_[ids[i]].x - ori_frame_old_.kpts_p2f_[id].x + 0.5),
                                    2) +
                                pow(floor(
                                        f1.kpts_p2f_[ids[i]].y - ori_frame_old_.kpts_p2f_[id].y + 0.5),
                                    2));
                        num++;
                    }
                }
            }
        }
        if (num > 20) {
            score_ = 100 - all / num;
            if (score_ < 0) score_ = 0.1;
            __android_log_print(ANDROID_LOG_INFO, "info", "all : %f, num %d score %f", all, num, score_);
        } else score_ = 0;

        cur_frame_.updateInlierNum();
        cur_frame_.inlier_num_ -= out_num_;
        cur_frame_.inlier_ref_num_ -= out_num_;
        std::chrono::steady_clock::time_point t2 = std::chrono::steady_clock::now();
        std::chrono::duration<double> time_used = std::chrono::duration_cast<std::chrono::duration<double>>(t2 - t1);
        
        if (!f2.is_ori_) {
            //h = h * f2.h_;
        }
        
        printf("RANSAC time %lf\n", time_used.count());
        printf("RANSAC out num : %d, in num : %d\n", out_num_, in_num_);
        __android_log_print(ANDROID_LOG_INFO, "info", "RANSAC time %lf RANSAC out num : %d in num : %d" , time_used.count(), out_num_, in_num_);
    }
    
    void Track::getDisToOriUseRect(cv::Mat &img, Frame &f1) {
        cv::Point2f pts[4];
        pts[0] = cv::Point2f(center_.x - img.cols * rect_scale_, center_.y - img.rows * rect_scale_);
        pts[1] = cv::Point2f(center_.x + img.cols * rect_scale_, center_.y - img.rows * rect_scale_);
        pts[2] = cv::Point2f(center_.x + img.cols * rect_scale_, center_.y + img.rows * rect_scale_);
        pts[3] = cv::Point2f(center_.x - img.cols * rect_scale_, center_.y + img.rows * rect_scale_);
        
        f1.dis2ori_ = 0;
        
        for (int i = 0; i < 4; i++) {
            cv::Point2f now = f1.getHPoint(pts[i]);
            f1.rect[i] = now;
            f1.dis2ori_ += std::sqrt(std::pow(now.x - pts[i].x, 2) + std::pow(now.y - pts[i].y, 2));
        }
    }
    
    cv::Mat Track::drawInfo(cv::Mat &img, Frame &f, const cv::Scalar &color) {
        cv::Mat output;
        img.copyTo(output);
        cv::Point2f pts[4];
        pts[0] = cv::Point2f(center_.x - img.cols * rect_scale_, center_.y - img.rows * rect_scale_ );
        pts[1] = cv::Point2f(center_.x + img.cols * rect_scale_, center_.y - img.rows * rect_scale_ );
        pts[2] = cv::Point2f(center_.x + img.cols * rect_scale_, center_.y + img.rows * rect_scale_ );
        pts[3] = cv::Point2f(center_.x - img.cols * rect_scale_, center_.y + img.rows * rect_scale_ );

        for (int i = 0; i < 4; i++) {
            cv::line(output, pts[i], pts[(i + 1) % 4], cv::Scalar(0, 0, 255), 3);
        }
        
        for (int i = 0; i < 4; i++) {
            if (method_ != 1)
            cv::line(output, f.getHPoint(pts[i]), f.getHPoint(pts[(i + 1) % 4]), color, 3);
            if (method_ != 0)
            cv::line(output, cur_frame_old_.getHPoint(pts[i]), cur_frame_old_.getHPoint(pts[(i + 1) % 4]), cv::Scalar(0, 255, 0), 3);
            if (!ref_frame_.is_ori_) cv::line(output, ref_frame_.getHPoint(pts[i]), ref_frame_.getHPoint(pts[(i + 1) % 4]), cv::Scalar(255, 255, 0), 3);
        }
        return output;
    }
    
    cv::Mat Track::drawMatch(cv::Mat &img, Frame &f) {
        cv::Mat output;
        std::vector<cv::DMatch> matches;
        std::vector<cv::KeyPoint> kpt1, kpt2;
        for (size_t i = 0; i < match_.cor_id_ori_.size(); i++) {
            int id = match_.cor_id_ori_[i];
            if (id != -1) {
                matches.push_back(cv::DMatch(i, id, 0));
            }
            kpt1.push_back(cv::KeyPoint(match_.kpts_p2f_[i], 1));
        }
        
        for (size_t i = 0; i < ori_frame_.kpts_kp_.size(); i++) {
            kpt2.push_back(cv::KeyPoint(ori_frame_.kpts_p2f_[i], 1));
        }
        
        cv::drawMatches(img, kpt1, ori_img_, kpt2, matches, output);
        return output;
    }

    double Track::getScore() {
        return score_;
    }

    void Track::clear() {
        cur_id_ = 1;
        top = 0;
        lk_model_ = 0;
        lk_init_num_ = -1;
    }
    void Track::updateD(int id, uchar *desc) {
        if (id == -1) return ;
        num_p_[id]++;
        if (num_p_[id] > 5 && lk_model_ == false) {
            num_p_[id] = 0;
            uchar *pp = ori_frame_.desc_.ptr(id);
            for (int i = 0; i < 32; i++) {
                pp[i] = uchar(desc[i]);
            }
        }
    }


}
