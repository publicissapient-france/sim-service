# Configure the AWS Provider
# debian base ami : ami-1d61a76a
# sim-service ami : ami-443e8f33
provider "aws" {
    access_key = "${var.access_key}"
    secret_key = "${var.secret_key}"
    region = "eu-west-1"
}

resource "aws_security_group" "simservice-sg" {
    name = "simservice-sg"
    description = "Allow all inbound traffic"

    ingress {
        from_port = 0
        to_port = 65535
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }
}

resource "aws_instance" "factory" {
    count = 3
    key_name = "sim-service"
    ami = "ami-443e8f33"
    instance_type = "m1.small"
    tags {
        Name = "SimService node#${count.index}"
        Project = "SimService"
    }
    subnet_id = "${aws_subnet.simservice-net.id}"

}

resource "aws_instance" "core" {
    key_name = "sim-service"
    ami = "ami-1d61a76a"
    instance_type = "m1.small"
    tags {
        Name = "SimService core"
        Project = "SimService"
    }
    subnet_id = "${aws_subnet.simservice-net.id}"
}

resource "aws_subnet" "simservice-net" {
  vpc_id = "vpc-6648a303"
  cidr_block = "172.30.3.0/24"
  map_public_ip_on_launch = true
  availability_zone = "eu-west-1b"
  tags {
          Name = "SimService subnet"
          Project = "SimService"
      }
}
