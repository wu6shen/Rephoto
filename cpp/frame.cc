#include <android/log.h>
#include "frame.h"

namespace reloc {
    Frame::Frame() : id_(-1) {}
    Frame::Frame(long id) : id_(id) {}
    Frame::Frame(long id, cv::Ptr<cv::ORB> orb) : id_(id), orb_(orb) {
        if (id == 0) {
            is_ori_ = true;
        } else {
            is_ori_ = false;
        }

        use_orb_t_ = false;
        
        kpts_kp_.clear();
        kpts_p2f_.clear();
        cor_id_ori_.clear();
        cor_id_ref_.clear();
    }
    
    void Frame::extractORB(cv::Mat &img) {
        std::chrono::steady_clock::time_point t1 = std::chrono::steady_clock::now();

        if (use_orb_t_) {
            if (orb_t_)
            __android_log_print(ANDROID_LOG_INFO, "info", "-----");
            (*orb_t_)(img, cv::Mat(), kpts_kp_, desc_);
            __android_log_print(ANDROID_LOG_INFO, "info", "-----");
        }
        else {
            orb_->detect(img, kpts_kp_);
            for (int i = kpts_kp_.size() - 1; i >= 0; i--) {
                if (kpts_kp_[i].pt.y > 500) kpts_kp_.erase(kpts_kp_.begin() + i);
            }
            orb_->compute(img, kpts_kp_, desc_);
        }
        
        std::chrono::steady_clock::time_point t2 = std::chrono::steady_clock::now();
        std::chrono::duration<double> time_used = std::chrono::duration_cast<std::chrono::duration<double>>(t2 - t1);
        
        kpts_p2f_.resize(kpts_kp_.size());
        cor_id_ori_.resize(kpts_kp_.size());
        cor_id_ref_.resize(kpts_kp_.size());
        
        for (size_t i = 0; i < kpts_kp_.size(); i++) {
            kpts_p2f_[i] = kpts_kp_[i].pt;
            cor_id_ori_[i] = cor_id_ref_[i] = -1;
        }
        
        printf("KeyPoint time use %lf\n", time_used.count());
        printf("frame %lu have %zu kpts\n", id_, kpts_kp_.size());
    }
    
    void Frame::updateCorUseKpt(std::vector<cv::DMatch> &matches, Frame &ref) {
        if (ref.is_ori_) {
            for (auto match : matches) {
                int rid = match.trainIdx;
                int mid = match.queryIdx;
                cor_id_ori_[mid] = cor_id_ref_[mid] = rid;
            }
        } else {
            for (auto match : matches) {
                int rid = match.trainIdx;
                int mid = match.queryIdx;
                cor_id_ref_[mid] = rid;
                cor_id_ori_[mid] = ref.cor_id_ori_[rid];
            }
        }
    }
    
    cv::Point2f Frame::getHPoint(const cv::Point2f &p) {
        double npt[3], pt[3];
        pt[0] = p.x, pt[1] = p.y, pt[2] = 1;
        for (int i = 0; i < 3; i++) {
            npt[i] = 0;
            for (int j = 0; j < 3; j++) {
                npt[i] += h_.at<double>(i, j) * pt[j];
            }
        }
        return cv::Point2f(npt[0] / npt[2], npt[1] / npt[2]);
    }
    
    double Frame::getError(const Frame &ori) {
        /*
        */
        double error = 0;
        int num = 0;
        for (size_t i = 0; i < cor_id_ori_.size(); i++) {
            int id = cor_id_ori_[i];
            if (id != -1) {
                cv::Point2f pt1 = ori.kpts_p2f_[id];
                cv::Point2f pt2 = kpts_p2f_[i];
                cv::Point2f pt3 = getHPoint(pt2);
                
                error += std::sqrt(std::pow(pt1.x - pt3.x, 2) + std::pow(pt1.y - pt3.y, 2));
                num++;
            }
        }
        printf("Cor Num : %d\n", num);
        return error / num;
    }
    
    double Frame::getDis(const Frame &f) {
        double dis = 0;
        for (int i = 0; i < 4; i++) {
            dis += std::sqrt(std::pow(f.rect[i].x - rect[i].x, 2) + std::pow(f.rect[i].y - rect[i].y, 2));
        }
        return dis;
    }
    
    
    void Frame::updateInlierNum() {
        inlier_num_ = 0;
        inlier_ref_num_ = 0;
        for (size_t i = 0; i < cor_id_ori_.size(); i++) {
            if (cor_id_ref_[i] != -1) inlier_ref_num_++;
            if (cor_id_ori_[i] != -1) inlier_num_++;
        }
    }

    void Frame::setORBT(ORBextractor *t) {
        orb_t_ = t;
        use_orb_t_ = true;
    }
}
